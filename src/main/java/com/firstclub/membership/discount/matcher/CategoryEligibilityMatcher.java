package com.firstclub.membership.discount.matcher;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import com.firstclub.membership.discount.dto.DiscountContext;
import org.springframework.stereotype.Component;

@Component
public class CategoryEligibilityMatcher implements EligibilityMatcher {

    @Override
    public EligibilityScopeType scopeType() {
        return EligibilityScopeType.PRODUCT_CATEGORY;
    }

    @Override
    public boolean matches(BenefitEligibility eligibility, DiscountContext context) {
        return context.categoryId() != null
                && context.categoryId().equals(eligibility.getScopeValue());
    }
}
