package com.firstclub.membership.discount.service;

import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import com.firstclub.membership.discount.dto.DiscountBadge;
import com.firstclub.membership.discount.dto.DiscountContext;
import com.firstclub.membership.discount.dto.DiscountResult;
import com.firstclub.membership.discount.dto.PlanDiscountView;
import com.firstclub.membership.discount.matcher.EligibilityMatcher;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.repository.MembershipPlanRepository;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import com.firstclub.membership.subscription.service.SubscriptionService;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Discount resolution service — two usage modes:
 *
 * <ul>
 *   <li><b>Product listing page</b>: {@link #listPlanDiscounts(String)} returns <em>all</em>
 *       active discount badges for the user's plan so the UI can annotate every product with the
 *       correct badge without making per-product API calls.</li>
 *   <li><b>Cart / checkout validation</b>: {@link #resolve(String, DiscountContext)} applies
 *       "most-specific-wins" to a single product and returns the exact discount percentage.</li>
 * </ul>
 *
 * Eligibility rows are served from cache, so both paths are suitable for high-traffic reads.
 */
@Service
public class DiscountResolutionServiceImpl implements DiscountResolutionService {

    private static final Logger log = LoggerFactory.getLogger(DiscountResolutionServiceImpl.class);

    private final SubscriptionService subscriptionService;
    private final MembershipPlanRepository planRepository;
    private final BenefitEligibilityService eligibilityService;
    private final Map<EligibilityScopeType, EligibilityMatcher> matchers =
            new EnumMap<>(EligibilityScopeType.class);

    public DiscountResolutionServiceImpl(SubscriptionService subscriptionService,
                                     MembershipPlanRepository planRepository,
                                     BenefitEligibilityService eligibilityService,
                                     List<EligibilityMatcher> matcherBeans) {
        this.subscriptionService = subscriptionService;
        this.planRepository = planRepository;
        this.eligibilityService = eligibilityService;
        matcherBeans.forEach(m -> matchers.put(m.scopeType(), m));
    }

    /**
     * <b>Product listing page</b>: returns every discount badge active for the user's plan,
     * pre-grouped by scope type so the frontend can build lookup maps without extra calls.
     *
     * <p>Frontend logic:
     * <ol>
     *   <li>Index {@code itemDiscounts} by {@code scopeValue} (SKU → DiscountBadge).</li>
     *   <li>Index {@code categoryDiscounts} by {@code scopeValue} (category → DiscountBadge).</li>
     *   <li>For each product: find item badge first (highest specificity), then category badge,
     *       then fall back to {@code allDiscounts} (plan-wide).</li>
     * </ol>
     */
    @Transactional(readOnly = true)
    public PlanDiscountView listPlanDiscounts(String userId) {
        log.debug("Listing plan discounts for user: {}", userId);
        Optional<SubscriptionResponse> activeSub = subscriptionService.findActiveSubscription(userId);
        if (activeSub.isEmpty()) {
            log.debug("No active subscription found for user: {}", userId);
            return PlanDiscountView.noActivePlan(userId);
        }

        Long planId = activeSub.get().planId();
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> ResourceNotFoundException.of("MembershipPlan", planId));

        List<BenefitEligibility> eligibilities = eligibilityService.forPlan(planId);

        List<DiscountBadge> all = badgesOf(eligibilities, EligibilityScopeType.ALL);
        List<DiscountBadge> category = badgesOf(eligibilities, EligibilityScopeType.PRODUCT_CATEGORY);
        List<DiscountBadge> item = badgesOf(eligibilities, EligibilityScopeType.PRODUCT_ITEM);

        log.debug("Found {} ALL, {} CATEGORY, and {} ITEM discounts for user: {} (plan: {})",
                 all.size(), category.size(), item.size(), userId, plan.getCode());
        return new PlanDiscountView(userId, planId, plan.getCode(), true, all, category, item);
    }

    /**
     * <b>Cart / checkout validation</b>: resolves the single best-matching discount for one
     * product using "most-specific-wins" (PRODUCT_ITEM > PRODUCT_CATEGORY > ALL).
     */
    @Transactional(readOnly = true)
    public DiscountResult resolve(String userId, DiscountContext context) {
        log.debug("Resolving discount for user: {} - product: {}, category: {}",
                 userId, context.productId(), context.categoryId());
        Optional<SubscriptionResponse> activeSub = subscriptionService.findActiveSubscription(userId);
        if (activeSub.isEmpty()) {
            log.debug("No active subscription for discount resolution - user: {}", userId);
            return DiscountResult.notEligible(userId, null, context, "No active membership subscription");
        }

        Long planId = activeSub.get().planId();
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> ResourceNotFoundException.of("MembershipPlan", planId));
        String planCode = plan.getCode();

        List<BenefitEligibility> eligibilities = eligibilityService.forPlan(planId);
        if (eligibilities.isEmpty()) {
            log.debug("No discount eligibilities configured for plan: {}", planCode);
            return DiscountResult.notEligible(userId, planCode, context,
                    "No discount eligibilities configured for plan " + planCode);
        }

        Optional<BenefitEligibility> best = eligibilities.stream()
                .filter(e -> {
                    EligibilityMatcher matcher = matchers.get(e.getScopeType());
                    return matcher != null && matcher.matches(e, context);
                })
                .max(Comparator
                        .comparingInt((BenefitEligibility e) -> e.getScopeType().specificity())
                        .thenComparing(e -> e.getDiscountPct()));

        if (best.isEmpty()) {
            log.debug("No discount eligibility matched for user: {} - product: {}, category: {}",
                     userId, context.productId(), context.categoryId());
            return DiscountResult.notEligible(userId, planCode, context,
                    "No discount eligibility matches this product/category for plan " + planCode);
        }

        BenefitEligibility match = best.get();
        log.info("Discount resolved for user: {} - productId: {}, categoryId: {}, discount: {}%, scope: {}",
                userId, context.productId(), context.categoryId(), match.getDiscountPct(), match.getScopeType());
        return new DiscountResult(userId, planCode, context.productId(), context.categoryId(),
                match.getDiscountPct().signum() > 0, match.getDiscountPct(),
                match.getScopeType(),
                "Matched " + match.getScopeType() + " eligibility on plan " + planCode);
    }

    private static List<DiscountBadge> badgesOf(List<BenefitEligibility> eligibilities,
                                                 EligibilityScopeType type) {
        return eligibilities.stream()
                .filter(e -> e.getScopeType() == type)
                .sorted(Comparator.comparing(e -> e.getScopeValue() == null ? "" : e.getScopeValue()))
                .map(e -> DiscountBadge.of(type, e.getScopeValue(), e.getDiscountPct()))
                .toList();
    }
}
