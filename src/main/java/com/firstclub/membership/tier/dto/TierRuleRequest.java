package com.firstclub.membership.tier.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TierRuleRequest(
        @NotNull(message = "targetTierId is required")
        Long targetTierId,

        @Min(value = 1, message = "minOrders must be at least 1 when provided")
        Integer minOrders,

        @DecimalMin(value = "0.01", message = "minMonthlyOrderValue must be positive when provided")
        BigDecimal minMonthlyOrderValue,

        @Size(max = 64, message = "cohort must not exceed 64 characters")
        String cohort,

        @Min(value = 1, message = "priority must be at least 1")
        Integer priority,

        Boolean active) {

    @AssertTrue(message = "At least one criterion must be set: minOrders, minMonthlyOrderValue, or cohort")
    public boolean isAtLeastOneCriterionSet() {
        return minOrders != null || minMonthlyOrderValue != null
                || (cohort != null && !cohort.isBlank());
    }
}
