package com.firstclub.membership.discount.dto;

/** The shopping-cart context a discount is evaluated against (extend with brand, seller, ...). */
public record DiscountContext(String productId, String categoryId) {
}
