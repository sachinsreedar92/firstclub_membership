package com.firstclub.membership.subscription.service;

import com.firstclub.membership.subscription.domain.SubscriptionHistory;
import com.firstclub.membership.subscription.dto.SubscribeRequest;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import java.util.List;
import java.util.Optional;

/**
 * Core subscription lifecycle service: subscribe, upgrade, downgrade, cancel, and auto tier
 * changes driven by the tier engine. Concurrency is handled with optimistic locking plus retry,
 * an idempotency key on subscribe, and the {@link SubscriptionStateMachine} guard for transitions.
 *
 * <p>Retried mutations run via a REQUIRES_NEW {@link TransactionTemplate} so each retry attempt
 * gets a fresh transaction (a failed optimistic lock marks the prior transaction rollback-only).
 */

public interface SubscriptionService {

    SubscriptionResponse subscribe(SubscribeRequest request, String idempotencyKey);
    SubscriptionResponse upgrade(Long subscriptionId, Long targetTierId);
    SubscriptionResponse downgrade(Long subscriptionId, Long targetTierId);
    void applyAutoTierChange(String userId, Long targetTierId);
    SubscriptionResponse cancel(Long subscriptionId);
    int expireDueSubscriptions();
    SubscriptionResponse getCurrentSubscription(String userId);
    Optional<SubscriptionResponse> findActiveSubscription(String userId);
    List<SubscriptionResponse> getSubscriptionHistory(String userId);
    List<SubscriptionHistory> getTierProgressionHistory(String userId);
}
