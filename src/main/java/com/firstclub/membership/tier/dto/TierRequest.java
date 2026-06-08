package com.firstclub.membership.tier.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TierRequest(
        @NotBlank(message = "code is required")
        @Size(max = 32, message = "code must not exceed 32 characters")
        String code,

        @NotBlank(message = "name is required")
        @Size(max = 128, message = "name must not exceed 128 characters")
        String name,

        @NotNull(message = "level is required")
        @Min(value = 1, message = "level must be at least 1")
        Integer level,

        Boolean active) {
}
