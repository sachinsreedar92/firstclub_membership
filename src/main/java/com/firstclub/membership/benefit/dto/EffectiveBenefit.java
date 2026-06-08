package com.firstclub.membership.benefit.dto;

import com.firstclub.membership.benefit.domain.BenefitType;
import java.math.BigDecimal;

/**
 * A resolved, presentation-ready benefit for a tier. Produced by the benefit resolver
 * chain so each benefit type can compute its own normalized value and human-readable label.
 */
public record EffectiveBenefit(
        BenefitType type,
        String code,
        String description,
        BigDecimal value,
        String displayValue) {
}
