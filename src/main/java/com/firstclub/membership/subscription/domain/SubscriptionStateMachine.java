package com.firstclub.membership.subscription.domain;

import com.firstclub.membership.common.exception.ConflictException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * State-pattern guard for subscription lifecycle transitions. The allowed actions per state are
 * declared in one table so illegal transitions (e.g., cancelling an already expired subscription,
 * or changing the tier of a cancelled one) are rejected consistently.
 */
@Component
public class SubscriptionStateMachine {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionStateMachine.class);

    private final Map<SubscriptionStatus, Set<SubscriptionAction>> allowed =
            new EnumMap<>(SubscriptionStatus.class);

    public SubscriptionStateMachine() {
        allowed.put(SubscriptionStatus.ACTIVE, Set.of(
                SubscriptionAction.UPGRADE,
                SubscriptionAction.DOWNGRADE,
                SubscriptionAction.TIER_AUTO_CHANGE,
                SubscriptionAction.RENEW,
                SubscriptionAction.CANCEL,
                SubscriptionAction.EXPIRE));
        allowed.put(SubscriptionStatus.CANCELLED, Set.of());
        allowed.put(SubscriptionStatus.EXPIRED, Set.of());
    }

    public boolean canApply(SubscriptionStatus current, SubscriptionAction action) {
        boolean allowed = this.allowed.getOrDefault(current, Set.of()).contains(action);
        log.debug("Checking if action {} is allowed from state {} - result: {}", action, current, allowed);
        return allowed;
    }

    public void assertCanApply(SubscriptionStatus current, SubscriptionAction action) {
        if (!canApply(current, action)) {
            log.warn("Invalid subscription state transition attempted - action: {}, current state: {}", action, current);
            throw new ConflictException(
                    "Action " + action + " is not allowed from state " + current);
        }
        log.debug("Subscription state transition validated - action: {}, current state: {}", action, current);
    }
}
