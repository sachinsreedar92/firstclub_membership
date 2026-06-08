package com.firstclub.membership.subscription.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically expires subscriptions past their expiry date, keeping membership state and the
 * single-active-subscription invariant accurate over time.
 */
@Component
public class SubscriptionExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryScheduler.class);

    private final SubscriptionService subscriptionService;

    public SubscriptionExpiryScheduler(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(fixedDelayString = "${membership.subscription.expiry-interval-ms:60000}")
    public void expireDue() {
        log.debug("Running subscription expiry check");
        int expiredCount = subscriptionService.expireDueSubscriptions();
        if (expiredCount > 0) {
            log.info("Subscription expiry scheduler completed - expired {} subscriptions", expiredCount);
        }
    }
}
