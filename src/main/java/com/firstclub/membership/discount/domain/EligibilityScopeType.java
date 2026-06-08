package com.firstclub.membership.discount.domain;

/**
 * Scope at which a plan's discount applies.  {@code specificity} drives "most-specific-wins"
 * resolution: a product-item rule beats a product-category rule, which beats the plan-wide (ALL)
 * rule.  New scopes (BRAND, SELLER, REGION …) are added here plus a matching
 * {@link com.firstclub.membership.discount.matcher.EligibilityMatcher} bean.
 */
public enum EligibilityScopeType {

    ALL(0),
    PRODUCT_CATEGORY(10),
    PRODUCT_ITEM(20);

    private final int specificity;

    EligibilityScopeType(int specificity) {
        this.specificity = specificity;
    }

    public int specificity() {
        return specificity;
    }
}
