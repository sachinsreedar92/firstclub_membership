package com.firstclub.membership.deal.dto;

import com.firstclub.membership.deal.domain.DealType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record DealRequest(
        @NotBlank(message = "code is required")
        @Size(max = 64, message = "code must not exceed 64 characters")
        String code,

        @NotBlank(message = "title is required")
        @Size(max = 160, message = "title must not exceed 160 characters")
        String title,

        @Size(max = 512, message = "description must not exceed 512 characters")
        String description,

        @NotNull(message = "type is required (EXCLUSIVE_DEAL or SALE)")
        DealType type,

        Instant publicStartAt,

        @NotNull(message = "endAt is required")
        @Future(message = "endAt must be a future date/time")
        Instant endAt,

        Boolean exclusive,
        Boolean active) {
}
