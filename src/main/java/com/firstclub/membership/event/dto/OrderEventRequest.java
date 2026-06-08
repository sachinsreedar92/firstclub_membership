package com.firstclub.membership.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Inbound order signal used to exercise the event-driven tier engine.  In production this would
 * arrive from the checkout service over a broker rather than an HTTP endpoint.
 */
public record OrderEventRequest(
        @NotBlank(message = "userId is required")
        @Size(max = 64, message = "userId must not exceed 64 characters")
        String userId,

        @Size(max = 64, message = "orderId must not exceed 64 characters")
        String orderId,

        @NotNull(message = "orderValue is required")
        @DecimalMin(value = "0.00", message = "orderValue must be zero or positive")
        BigDecimal orderValue,

        @Size(max = 64, message = "cohort must not exceed 64 characters")
        String cohort) {
}
