package com.firstclub.membership.discount.dto;

import com.firstclub.membership.discount.domain.EligibilityScopeType;
import java.math.BigDecimal;

/**
 * A single discount rule that the product-listing page uses to annotate items with a badge.
 *
 * <ul>
 *   <li>{@code scopeType = ALL}              → apply badge to every product</li>
 *   <li>{@code scopeType = PRODUCT_CATEGORY} → apply badge to every product in {@code scopeValue}</li>
 *   <li>{@code scopeType = PRODUCT_ITEM}     → apply badge to the specific SKU {@code scopeValue}</li>
 * </ul>
 *
 * The most-specific matching badge should win when a product matches multiple scopes.
 */
public record DiscountBadge(
        EligibilityScopeType scopeType,

        /**
         * Category ID for PRODUCT_CATEGORY, product/SKU ID for PRODUCT_ITEM, null for ALL.
         */
        String scopeValue,

        BigDecimal discountPct,

        /** Human-readable label ready to render in a product badge, e.g. "15% off Electronics". */
        String label) {

    public static DiscountBadge of(EligibilityScopeType scopeType,
                                   String scopeValue, BigDecimal discountPct) {
        String label = switch (scopeType) {
            case ALL -> discountPct + "% off all items";
            case PRODUCT_CATEGORY -> discountPct + "% off on " + scopeValue;
            case PRODUCT_ITEM -> discountPct + "% off on item " + scopeValue;
        };
        return new DiscountBadge(scopeType, scopeValue, discountPct, label);
    }
}
