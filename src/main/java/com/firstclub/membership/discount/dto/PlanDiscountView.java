package com.firstclub.membership.discount.dto;

import java.util.List;

/**
 * All discount badges active for a member's plan — designed for the <b>product listing page</b>.
 *
 * <p>The frontend iterates {@code discounts} once and, for every product, picks the most-specific
 * matching badge (PRODUCT_ITEM > PRODUCT_CATEGORY > ALL) to render alongside the item price.
 * Grouping by {@code scopeType} makes it easy to build per-category or per-item lookup maps
 * without additional API calls per product.
 */
public record PlanDiscountView(
        String userId,
        Long planId,
        String planCode,
        boolean hasActivePlan,
        List<DiscountBadge> allDiscounts,
        List<DiscountBadge> categoryDiscounts,
        List<DiscountBadge> itemDiscounts) {

    public static PlanDiscountView noActivePlan(String userId) {
        return new PlanDiscountView(userId, null, null, false,
                List.of(), List.of(), List.of());
    }
}
