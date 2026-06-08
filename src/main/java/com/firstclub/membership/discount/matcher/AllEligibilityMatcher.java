package com.firstclub.membership.discount.matcher;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import com.firstclub.membership.discount.dto.DiscountContext;
import org.springframework.stereotype.Component;

@Component
public class AllEligibilityMatcher implements EligibilityMatcher {

    @Override
    public EligibilityScopeType scopeType() {
        return EligibilityScopeType.ALL;
    }

    @Override
    public boolean matches(BenefitEligibility eligibility, DiscountContext context) {
        return true;
    }
}
