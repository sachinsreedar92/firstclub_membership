package com.firstclub.membership.common.cache;

import java.util.List;

/**
 * Central registry of cache names. Keeping them in one place lets the cache manager,
 * the cached services, and the cache-reload API stay consistent and discoverable.
 */
public final class CacheNames {

    public static final String PLANS = "plans";
    public static final String TIERS = "tiers";
    public static final String BENEFIT_DEFINITIONS = "benefitDefinitions";
    public static final String TIER_BENEFITS = "tierBenefits";
    public static final String TIER_RULES = "tierRules";
    public static final String BENEFIT_ELIGIBILITIES = "benefitEligibilities";
    public static final String DEALS = "deals";
    public static final String DEAL_TIER_ACCESS = "dealTierAccess";

    public static final List<String> ALL = List.of(
            PLANS, TIERS, BENEFIT_DEFINITIONS, TIER_BENEFITS, TIER_RULES,
            BENEFIT_ELIGIBILITIES, DEALS, DEAL_TIER_ACCESS);

    private CacheNames() {
    }
}
