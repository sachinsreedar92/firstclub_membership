package com.firstclub.membership.tier.dto;

import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import com.firstclub.membership.tier.domain.Tier;
import java.util.List;

public record TierResponse(
        Long id,
        String code,
        String name,
        int level,
        boolean active,
        List<EffectiveBenefit> benefits) {

    public static TierResponse from(Tier tier, List<EffectiveBenefit> benefits) {
        return new TierResponse(
                tier.getId(), tier.getCode(), tier.getName(),
                tier.getLevel(), tier.isActive(), benefits);
    }
}
