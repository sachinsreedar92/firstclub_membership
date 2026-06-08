package com.firstclub.membership.member.service;

import com.firstclub.membership.tier.evaluation.TierEvaluationContext;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Maintains rolling per-user order statistics from inbound order events. Updates use optimistic
 * locking with retry so concurrent order events for the same user are applied correctly without
 * pessimistic locks (high-concurrency friendly).
 */

public interface OrderStatsService {

    TierEvaluationContext applyOrder(String userId, BigDecimal orderValue, Instant occurredAt);
    TierEvaluationContext currentContext(String userId);
}
