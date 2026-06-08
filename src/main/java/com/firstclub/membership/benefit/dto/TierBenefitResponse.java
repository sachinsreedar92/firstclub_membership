package com.firstclub.membership.benefit.dto;

import com.firstclub.membership.benefit.domain.BenefitType;
import com.firstclub.membership.benefit.domain.TierBenefit;
import java.math.BigDecimal;

public record TierBenefitResponse(
        Long id,
        Long tierId,
        Long benefitDefinitionId,
        BenefitType type,
        String code,
        BigDecimal numericValue,
        String configJson,
        boolean active) {

    public static TierBenefitResponse from(TierBenefit tb) {
        return new TierBenefitResponse(
                tb.getId(),
                tb.getTier().getId(),
                tb.getBenefitDefinition().getId(),
                tb.getBenefitDefinition().getType(),
                tb.getBenefitDefinition().getCode(),
                tb.getNumericValue(),
                tb.getConfigJson(),
                tb.isActive());
    }
}
