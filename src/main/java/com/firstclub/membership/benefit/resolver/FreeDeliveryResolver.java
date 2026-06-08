package com.firstclub.membership.benefit.resolver;

import com.firstclub.membership.benefit.domain.BenefitType;
import com.firstclub.membership.benefit.domain.TierBenefit;
import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FreeDeliveryResolver implements BenefitResolver {

    @Override
    public boolean supports(BenefitType type) {
        return type == BenefitType.FREE_DELIVERY;
    }

    @Override
    public EffectiveBenefit resolve(TierBenefit tb) {
        BigDecimal threshold = tb.getNumericValue();
        String display = (threshold == null || threshold.signum() == 0)
                ? "Free delivery on all eligible orders"
                : "Free delivery on orders above " + threshold.stripTrailingZeros().toPlainString();
        return new EffectiveBenefit(
                BenefitType.FREE_DELIVERY,
                tb.getBenefitDefinition().getCode(),
                tb.getBenefitDefinition().getDescription(),
                threshold,
                display);
    }
}
