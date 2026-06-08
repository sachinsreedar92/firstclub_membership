package com.firstclub.membership.member.service;

import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import com.firstclub.membership.benefit.service.BenefitService;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.member.domain.Member;
import com.firstclub.membership.member.dto.MembershipView;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.repository.MembershipPlanRepository;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import com.firstclub.membership.subscription.service.SubscriptionService;
import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.service.TierService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade that composes a member's profile, active subscription, current plan, current tier and
 * resolved benefits into a single view.  Keeps controllers thin and hides cross-feature wiring
 * behind one entry point (Facade pattern).
 */
@Service
public class MembershipFacadeImpl implements MembershipFacade {

    private static final Logger log = LoggerFactory.getLogger(MembershipFacadeImpl.class);

    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final TierService tierService;
    private final BenefitService benefitService;
    private final MembershipPlanRepository planRepository;

    public MembershipFacadeImpl(MemberService memberService,
                            SubscriptionService subscriptionService,
                            TierService tierService,
                            BenefitService benefitService,
                            MembershipPlanRepository planRepository) {
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
        this.tierService = tierService;
        this.benefitService = benefitService;
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public MembershipView getMembership(String userId) {
        log.debug("Getting membership view for user: {}", userId);
        Member member = memberService.findMember(userId);
        Optional<SubscriptionResponse> active = subscriptionService.findActiveSubscription(userId);

        Long planId = active.map(SubscriptionResponse::planId).orElse(null);
        String planCode = null;
        if (planId != null) {
            planCode = planRepository.findById(planId)
                    .map(MembershipPlan::getCode).orElse(null);
        }

        Long currentTierId = active.map(SubscriptionResponse::tierId)
                .orElseGet(() -> member == null ? null : member.getCurrentTierId());

        String tierCode = null;
        List<EffectiveBenefit> benefits = List.of();
        if (currentTierId != null) {
            Tier tier = tierService.requireTier(currentTierId);
            tierCode = tier.getCode();
            benefits = benefitService.effectiveBenefitsForTier(currentTierId);
        }

        log.debug("Membership view retrieved for user: {} - planCode: {}, tierCode: {}, hasActiveSubscription: {}",
                 userId, planCode, tierCode, active.isPresent());
        return new MembershipView(
                userId,
                member == null ? null : member.getCohort(),
                planId,
                planCode,
                currentTierId,
                tierCode,
                active.isPresent(),
                active.orElse(null),
                benefits);
    }

    @Transactional(readOnly = true)
    public List<EffectiveBenefit> getMemberBenefits(String userId) {
        log.debug("Getting member benefits for user: {}", userId);
        MembershipView view = getMembership(userId);
        if (view.currentTierId() == null) {
            log.warn("No tier/benefits found for user: {}", userId);
            throw new ResourceNotFoundException("No tier/benefits for user: " + userId);
        }
        log.debug("Found {} benefits for user: {}", view.benefits().size(), userId);
        return view.benefits();
    }
}
