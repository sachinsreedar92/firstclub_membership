package com.firstclub.membership.deal.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record DealTierAccessRequest(
        @NotNull(message = "planId is required")
        Long planId,

        @NotNull(message = "accessStartAt is required")
        Instant accessStartAt) {
}
