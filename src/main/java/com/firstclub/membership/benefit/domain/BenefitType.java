package com.firstclub.membership.benefit.domain;

/**
 * Catalog of benefit categories a tier can unlock. New perk categories are added
 * here; concrete per-tier values live in {@code TierBenefit}, keeping perks configurable.
 */
public enum BenefitType {
    FREE_DELIVERY,
    EXTRA_DISCOUNT,
    EXCLUSIVE_DEALS,
    EARLY_ACCESS,
    PRIORITY_SUPPORT,
    FULL_REFUND
}
