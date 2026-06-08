package com.firstclub.membership.benefit.dto;

import com.firstclub.membership.benefit.domain.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BenefitDefinitionRequest(
        @NotBlank(message = "code is required")
        @Size(max = 64, message = "code must not exceed 64 characters")
        String code,

        @NotNull(message = "type is required (FREE_DELIVERY, EXTRA_DISCOUNT, EXCLUSIVE_DEALS, EARLY_ACCESS, PRIORITY_SUPPORT)")
        BenefitType type,

        @NotBlank(message = "description is required")
        @Size(max = 256, message = "description must not exceed 256 characters")
        String description,

        Boolean active) {
}
