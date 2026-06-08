package com.firstclub.membership.benefit.resolver;

import com.firstclub.membership.benefit.domain.BenefitType;
import com.firstclub.membership.benefit.domain.TierBenefit;
import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Handles boolean-style perks that are either granted or not (exclusive deals, early access,
 * priority support). One resolver covers several types to avoid boilerplate while still being
 * easy to split later if any of them needs richer configuration.
 */
@Component
public class SimpleFlagBenefitResolver implements BenefitResolver {

    private static final Set<BenefitType> FLAG_TYPES = Set.of(
            BenefitType.EXCLUSIVE_DEALS,
            BenefitType.EARLY_ACCESS,
            BenefitType.PRIORITY_SUPPORT);

    @Override
    public boolean supports(BenefitType type) {
        return FLAG_TYPES.contains(type);
    }

    @Override
    public EffectiveBenefit resolve(TierBenefit tb) {
        return new EffectiveBenefit(
                tb.getBenefitDefinition().getType(),
                tb.getBenefitDefinition().getCode(),
                tb.getBenefitDefinition().getDescription(),
                null,
                "Included");
    }
}
