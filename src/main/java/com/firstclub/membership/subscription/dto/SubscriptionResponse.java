package com.firstclub.membership.subscription.dto;

import com.firstclub.membership.subscription.domain.Subscription;
import com.firstclub.membership.subscription.domain.SubscriptionStatus;
import java.time.Duration;
import java.time.Instant;

public record SubscriptionResponse(
        Long id,
        String userId,
        Long planId,
        Long tierId,
        SubscriptionStatus status,
        Instant startDate,
        Instant expiryDate,
        boolean autoRenew,
        long daysRemaining) {

    public static SubscriptionResponse from(Subscription s) {
        long days = 0;
        if (s.getStatus() == SubscriptionStatus.ACTIVE && s.getExpiryDate() != null) {
            days = Math.max(0, Duration.between(Instant.now(), s.getExpiryDate()).toDays());
        }
        return new SubscriptionResponse(
                s.getId(),
                s.getUserId(),
                s.getPlanId(),
                s.getTierId(),
                s.getStatus(),
                s.getStartDate(),
                s.getExpiryDate(),
                s.isAutoRenew(),
                days);
    }
}
