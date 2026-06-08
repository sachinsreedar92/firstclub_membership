package com.firstclub.membership.deal.domain;

/**
 * EXCLUSIVE_DEAL: visible only to tiers granted access. SALE: generally available from a public
 * start time, but eligible tiers can be granted earlier access (early access to sales).
 */
public enum DealType {
    EXCLUSIVE_DEAL,
    SALE,
    EMPLOYEE_DEAL
}
