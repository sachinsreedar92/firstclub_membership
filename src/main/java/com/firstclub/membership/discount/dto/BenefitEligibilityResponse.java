package com.firstclub.membership.discount.dto;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import java.math.BigDecimal;

public record BenefitEligibilityResponse(
        Long id,
        Long planId,
        String planCode,
        EligibilityScopeType scopeType,
        String scopeValue,
        BigDecimal discountPct,
        boolean active) {

    public static BenefitEligibilityResponse from(BenefitEligibility e) {
        return new BenefitEligibilityResponse(
                e.getId(),
                e.getMembershipPlan().getId(),
                e.getMembershipPlan().getCode(),
                e.getScopeType(),
                e.getScopeValue(),
                e.getDiscountPct(),
                e.isActive());
    }
}
