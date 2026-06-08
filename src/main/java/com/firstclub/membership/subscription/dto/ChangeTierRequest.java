package com.firstclub.membership.subscription.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeTierRequest(
        @NotNull Long targetTierId) {
}
