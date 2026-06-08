package com.firstclub.membership.deal.service;

import com.firstclub.membership.common.cache.CacheNames;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.deal.domain.Deal;
import com.firstclub.membership.deal.domain.DealPlanAccess;
import com.firstclub.membership.deal.dto.CachedDeal;
import com.firstclub.membership.deal.dto.DealRequest;
import com.firstclub.membership.deal.dto.DealResponse;
import com.firstclub.membership.deal.dto.DealTierAccessRequest;
import com.firstclub.membership.deal.dto.PublicDealView;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.deal.repository.DealPlanAccessRepository;
import com.firstclub.membership.deal.repository.DealRepository;
import com.firstclub.membership.plan.repository.MembershipPlanRepository;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import com.firstclub.membership.subscription.service.SubscriptionService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves which deals a member can currently access based on their membership plan and time.
 *
 * <p>Deals are plan-based (not tier-based): exclusive deals and early access are configured per
 * plan via {@link DealPlanAccess} rows, so premium plans receive earlier access timestamps.
 * The active deal catalog is cached as immutable snapshots for fast reads on the hot path.
 */
@Service
public class DealServiceImpl implements DealService {

    private static final Logger log = LoggerFactory.getLogger(DealServiceImpl.class);

    private final DealRepository dealRepository;
    private final DealPlanAccessRepository dealPlanAccessRepository;
    private final MembershipPlanRepository planRepository;
    private final SubscriptionService subscriptionService;

    public DealServiceImpl(DealRepository dealRepository,
                       DealPlanAccessRepository dealPlanAccessRepository,
                       MembershipPlanRepository planRepository,
                       SubscriptionService subscriptionService) {
        this.dealRepository = dealRepository;
        this.dealPlanAccessRepository = dealPlanAccessRepository;
        this.planRepository = planRepository;
        this.subscriptionService = subscriptionService;
    }

    @Cacheable(cacheNames = CacheNames.DEALS, key = "'active'")
    @Transactional(readOnly = true)
    public List<CachedDeal> activeDeals() {
        log.debug("Retrieving active deals from database");
        List<CachedDeal> deals = dealRepository.findByActiveTrue().stream()
                .map(deal -> {
                    Map<Long, Instant> access = dealPlanAccessRepository.findByDealId(deal.getId())
                            .stream()
                            .collect(Collectors.toMap(
                                    DealPlanAccess::getPlanId, DealPlanAccess::getAccessStartAt));
                    return new CachedDeal(deal.getId(), deal.getCode(), deal.getTitle(),
                            deal.getDescription(), deal.getType(), deal.getPublicStartAt(),
                            deal.getEndAt(), deal.isExclusive(), access);
                })
                .toList();
        log.debug("Found {} active deals", deals.size());
        return deals;
    }

    @Transactional(readOnly = true)
    public List<DealResponse> accessibleDeals(String userId) {
        log.debug("Retrieving accessible deals for user: {}", userId);
        Optional<SubscriptionResponse> activeSub = subscriptionService.findActiveSubscription(userId);
        Long planId = activeSub.map(SubscriptionResponse::planId).orElse(null);
        Instant now = Instant.now();

        List<DealResponse> deals = activeDeals().stream()
                .filter(deal -> now.isBefore(deal.endAt()))
                .map(deal -> toAccessibleResponse(deal, planId, now))
                .filter(java.util.Objects::nonNull)
                .toList();
        log.debug("Found {} accessible deals for user: {} (planId: {})", deals.size(), userId, planId);
        return deals;
    }

    private DealResponse toAccessibleResponse(CachedDeal deal, Long planId, Instant now) {
        Instant planAccessStart = planId == null ? null : deal.planAccessStartById().get(planId);

        // Plan-specific access covers exclusive deals and early access to sales.
        if (planAccessStart != null && !now.isBefore(planAccessStart)) {
            boolean earlyAccess = deal.publicStartAt() != null
                    && planAccessStart.isBefore(deal.publicStartAt())
                    && now.isBefore(deal.publicStartAt());
            return new DealResponse(deal.id(), deal.code(), deal.title(), deal.description(),
                    deal.type(), deal.endAt(), earlyAccess, planAccessStart);
        }

        // Public access for non-exclusive deals once the public start has passed.
        if (!deal.exclusive() && deal.publicStartAt() != null && !now.isBefore(deal.publicStartAt())) {
            return new DealResponse(deal.id(), deal.code(), deal.title(), deal.description(),
                    deal.type(), deal.endAt(), false, deal.publicStartAt());
        }

        return null;
    }

    /**
     * <b>Product listing / promotions page</b> — returns every deal (active or upcoming) with
     * the full per-plan access schedule so the UI can render:
     * "Exclusive for Yearly / Quarterly", "Early access: Yearly now, Quarterly tomorrow, public in 2 days".
     */
    @Transactional(readOnly = true)
    public List<PublicDealView> listAllDeals() {
        log.debug("Listing all deals (active and upcoming)");
        List<PublicDealView> deals = dealRepository.findAll().stream()
                .map(this::toPublicView)
                .toList();
        log.debug("Found {} total deals", deals.size());
        return deals;
    }

    private PublicDealView toPublicView(Deal deal) {
        List<DealPlanAccess> accesses = dealPlanAccessRepository.findByDealId(deal.getId());
        List<PublicDealView.PlanAccessInfo> planAccessInfos = accesses.stream()
                .map(a -> {
                    String planCode = planRepository.findById(a.getPlanId())
                            .map(MembershipPlan::getCode).orElse("(unknown)");
                    boolean earlyAccess = deal.getPublicStartAt() != null
                            && a.getAccessStartAt().isBefore(deal.getPublicStartAt());
                    return new PublicDealView.PlanAccessInfo(
                            a.getPlanId(), planCode, a.getAccessStartAt(), earlyAccess);
                })
                .toList();
        return new PublicDealView(
                deal.getId(), deal.getCode(), deal.getTitle(), deal.getDescription(),
                deal.getType(), deal.isExclusive(), deal.getPublicStartAt(),
                deal.getEndAt(), deal.isActive(), planAccessInfos);
    }

    @CacheEvict(cacheNames = CacheNames.DEALS, allEntries = true)
    @Transactional
    public Deal createDeal(DealRequest request) {
        log.info("Creating new deal with code: {}", request.code());
        dealRepository.findByCode(request.code()).ifPresent(d -> {
            log.warn("Deal code already exists: {}", request.code());
            throw new ConflictException("Deal code already exists: " + request.code());
        });
        Deal deal = new Deal(
                request.code(), request.title(), request.description(), request.type(),
                request.publicStartAt(), request.endAt(),
                request.exclusive() != null && request.exclusive(),
                request.active() == null || request.active());
        Deal saved = dealRepository.save(deal);
        log.info("Deal created successfully - id={}, code={}, title={}", saved.getId(), saved.getCode(), saved.getTitle());
        return saved;
    }

    @CacheEvict(cacheNames = CacheNames.DEALS, allEntries = true)
    @Transactional
    public DealPlanAccess grantPlanAccess(Long dealId, DealTierAccessRequest request) {
        log.info("Granting plan access - dealId: {}, planId: {}, accessStartAt: {}",
                 dealId, request.planId(), request.accessStartAt());
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> {
                    log.warn("Deal not found: {}", dealId);
                    return ResourceNotFoundException.of("Deal", dealId);
                });
        planRepository.findById(request.planId())
                .orElseThrow(() -> {
                    log.warn("Membership plan not found: {}", request.planId());
                    return ResourceNotFoundException.of("MembershipPlan", request.planId());
                });
        DealPlanAccess access = dealPlanAccessRepository.save(
                new DealPlanAccess(deal.getId(), request.planId(), request.accessStartAt()));
        log.info("Plan access granted successfully - dealId: {}, planId: {}", dealId, request.planId());
        return access;
    }
}
