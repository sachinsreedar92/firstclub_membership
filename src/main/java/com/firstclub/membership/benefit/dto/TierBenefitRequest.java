package com.firstclub.membership.benefit.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TierBenefitRequest(
        @NotNull(message = "tierId is required")
        Long tierId,

        @NotNull(message = "benefitDefinitionId is required")
        Long benefitDefinitionId,

        @DecimalMin(value = "0.00", message = "numericValue must be zero or positive")
        @DecimalMax(value = "100.00", message = "numericValue must not exceed 100")
        BigDecimal numericValue,

      /*  @Size(max = 1024, message = "configJson must not exceed 1024 characters")
        String configJson,*/

        Boolean active) {
}
