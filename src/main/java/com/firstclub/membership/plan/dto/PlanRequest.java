package com.firstclub.membership.plan.dto;

import com.firstclub.membership.plan.domain.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PlanRequest(
        @NotBlank(message = "code is required")
        @Size(max = 32, message = "code must not exceed 32 characters")
        String code,

        @NotBlank(message = "name is required")
        @Size(max = 128, message = "name must not exceed 128 characters")
        String name,

        @NotNull(message = "billingCycle is required (MONTHLY, QUARTERLY, or YEARLY)")
        BillingCycle billingCycle,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be greater than 0")
        BigDecimal price,

        @Size(max = 8, message = "currency must not exceed 8 characters")
        String currency,

        Integer durationDays,

        Boolean active) {
}
