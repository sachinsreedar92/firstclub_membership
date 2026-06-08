package com.firstclub.membership.tier.service;

import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.domain.TierRule;
import com.firstclub.membership.tier.dto.TierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.TierRuleRequest;
import com.firstclub.membership.tier.dto.TierRuleResponse;
import java.util.List;

/**
 * Catalog and configuration service for tiers and their progression rules. Tier lists
 * (with resolved benefits) and active rule sets are cached, since they are read on hot paths
 * (tier listing, tier evaluation) and only change via admin writes.
 */

public interface TierService {

    List<TierResponse> listActiveTiers();
    Tier requireTier(Long tierId);
    String findTierCode(Long tierId);
    TierResponse getTier(Long tierId);
    TierResponse createTier(TierRequest request);
    List<TierRule> activeRules();
    List<TierRuleResponse> listRules();
    TierRuleResponse createRule(TierRuleRequest request);
}
