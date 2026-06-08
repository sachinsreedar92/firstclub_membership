package com.firstclub.membership.benefit.resolver;

import com.firstclub.membership.benefit.domain.BenefitType;
import com.firstclub.membership.benefit.domain.TierBenefit;
import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ExtraDiscountResolver implements BenefitResolver {

    @Override
    public boolean supports(BenefitType type) {
        return type == BenefitType.EXTRA_DISCOUNT;
    }

    @Override
    public EffectiveBenefit resolve(TierBenefit tb) {
        BigDecimal percent = tb.getNumericValue() == null ? BigDecimal.ZERO : tb.getNumericValue();
        return new EffectiveBenefit(
                BenefitType.EXTRA_DISCOUNT,
                tb.getBenefitDefinition().getCode(),
                tb.getBenefitDefinition().getDescription(),
                percent,
                percent.stripTrailingZeros().toPlainString() + "% extra discount");
    }
}
