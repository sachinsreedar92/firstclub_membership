package com.firstclub.membership.tier.dto;

import com.firstclub.membership.tier.domain.TierRule;
import java.math.BigDecimal;

public record TierRuleResponse(
        Long id,
        Long targetTierId,
        String targetTierCode,
        Integer minOrders,
        BigDecimal minMonthlyOrderValue,
        String cohort,
        int priority,
        boolean active) {

    public static TierRuleResponse from(TierRule rule) {
        return new TierRuleResponse(
                rule.getId(),
                rule.getTargetTier().getId(),
                rule.getTargetTier().getCode(),
                rule.getMinOrders(),
                rule.getMinMonthlyOrderValue(),
                rule.getCohort(),
                rule.getPriority(),
                rule.isActive());
    }
}
