package com.firstclub.membership.member.dto;

import com.firstclub.membership.subscription.domain.SubscriptionAction;
import java.time.Instant;

/** A single tier-change event in a member's progression history. */
public record TierProgressionEvent(
        Long subscriptionHistoryId,
        SubscriptionAction action,
        Long fromTierId,
        String fromTierCode,
        Long toTierId,
        String toTierCode,
        String note,
        Instant occurredAt) {
}
