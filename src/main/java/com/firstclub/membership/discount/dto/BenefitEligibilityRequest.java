package com.firstclub.membership.discount.dto;

import com.firstclub.membership.discount.domain.EligibilityScopeType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record BenefitEligibilityRequest(
        @NotNull(message = "planId is required")
        Long planId,

        @NotNull(message = "scopeType is required (ALL, PRODUCT_CATEGORY, or PRODUCT_ITEM)")
        EligibilityScopeType scopeType,

        @Size(max = 128, message = "scopeValue must not exceed 128 characters")
        String scopeValue,

        @NotNull(message = "discountPct is required")
        @DecimalMin(value = "0.01", message = "discountPct must be at least 0.01%")
        @DecimalMax(value = "100.00", message = "discountPct must not exceed 100%")
        BigDecimal discountPct,

        Boolean active) {
}
