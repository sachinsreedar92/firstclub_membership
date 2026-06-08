package com.firstclub.membership.member.dto;

import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import java.util.List;

/**
 * Composite read model for a member's current membership: profile, active plan, current tier,
 * active subscription (if any), and the resolved benefits.  Built by the membership facade.
 */
public record MembershipView(
        String userId,
        String cohort,
        Long planId,
        String planCode,
        Long currentTierId,
        String currentTierCode,
        boolean hasActiveSubscription,
        SubscriptionResponse subscription,
        List<EffectiveBenefit> benefits) {
}
