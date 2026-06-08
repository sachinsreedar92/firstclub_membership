package com.firstclub.membership.deal.dto;

import com.firstclub.membership.deal.domain.DealType;
import java.time.Instant;

public record DealResponse(
        Long id,
        String code,
        String title,
        String description,
        DealType type,
        Instant endAt,
        boolean earlyAccess,
        Instant accessibleSince) {
}
