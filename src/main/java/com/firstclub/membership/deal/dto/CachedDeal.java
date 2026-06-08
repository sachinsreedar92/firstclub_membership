package com.firstclub.membership.deal.dto;

import com.firstclub.membership.deal.domain.DealType;
import java.time.Instant;
import java.util.Map;

/**
 * Cache-friendly snapshot of a deal plus its per-plan access times.  Using an immutable record
 * (no JPA lazy associations) makes it safe to cache and read outside a transaction on the hot path.
 */
public record CachedDeal(
        Long id,
        String code,
        String title,
        String description,
        DealType type,
        Instant publicStartAt,
        Instant endAt,
        boolean exclusive,
        Map<Long, Instant> planAccessStartById) {
}
