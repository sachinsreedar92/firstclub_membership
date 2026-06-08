package com.firstclub.membership.plan.domain;

/**
 * Supported membership billing cycles. The default duration is used when a plan
 * does not override it, keeping plan creation simple while remaining flexible.
 */
public enum BillingCycle {
    MONTHLY(30),
    QUARTERLY(90),
    YEARLY(365);

    private final int defaultDurationDays;

    BillingCycle(int defaultDurationDays) {
        this.defaultDurationDays = defaultDurationDays;
    }

    public int defaultDurationDays() {
        return defaultDurationDays;
    }
}
