package com.firstclub.membership.benefit.dto;

import com.firstclub.membership.benefit.domain.BenefitDefinition;
import com.firstclub.membership.benefit.domain.BenefitType;

public record BenefitDefinitionResponse(
        Long id,
        String code,
        BenefitType type,
        String description,
        boolean active) {

    public static BenefitDefinitionResponse from(BenefitDefinition def) {
        return new BenefitDefinitionResponse(
                def.getId(), def.getCode(), def.getType(), def.getDescription(), def.isActive());
    }
}
