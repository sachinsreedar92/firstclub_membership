package com.firstclub.membership.discount.matcher;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import com.firstclub.membership.discount.dto.DiscountContext;
import org.springframework.stereotype.Component;

@Component
public class ProductEligibilityMatcher implements EligibilityMatcher {

    @Override
    public EligibilityScopeType scopeType() {
        return EligibilityScopeType.PRODUCT_ITEM;
    }

    @Override
    public boolean matches(BenefitEligibility eligibility, DiscountContext context) {
        return context.productId() != null
                && context.productId().equals(eligibility.getScopeValue());
    }
}
