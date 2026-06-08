package com.firstclub.membership.discount.dto;

import com.firstclub.membership.discount.domain.EligibilityScopeType;
import java.math.BigDecimal;

public record DiscountResult(
        String userId,
        String planCode,
        String productId,
        String categoryId,
        boolean eligible,
        BigDecimal discountPercentage,
        EligibilityScopeType matchedScope,
        String reason) {

    public static DiscountResult notEligible(String userId, String planCode,
                                             DiscountContext ctx, String reason) {
        return new DiscountResult(userId, planCode, ctx.productId(), ctx.categoryId(),
                false, BigDecimal.ZERO, null, reason);
    }
}
