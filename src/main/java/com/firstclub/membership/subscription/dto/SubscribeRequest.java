package com.firstclub.membership.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubscribeRequest(
        @NotBlank(message = "userId is required")
        @Size(max = 64, message = "userId must not exceed 64 characters")
        String userId,

        @NotNull(message = "planId is required")
        Long planId,

        @NotNull(message = "tierId is required")
        Long tierId,

        Boolean autoRenew) {
}
