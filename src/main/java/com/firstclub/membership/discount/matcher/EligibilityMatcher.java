package com.firstclub.membership.discount.matcher;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import com.firstclub.membership.discount.dto.DiscountContext;

/**
 * Strategy that decides whether an eligibility rule of a given scope applies to a discount context.
 * Supporting a new scope (e.g., BRAND) is purely additive: add an enum value and a matcher bean.
 */
public interface EligibilityMatcher {

    EligibilityScopeType scopeType();

    boolean matches(BenefitEligibility eligibility, DiscountContext context);
}
