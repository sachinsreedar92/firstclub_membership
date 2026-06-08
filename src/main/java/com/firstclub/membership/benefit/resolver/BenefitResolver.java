package com.firstclub.membership.benefit.resolver;

import com.firstclub.membership.benefit.domain.BenefitType;
import com.firstclub.membership.benefit.domain.TierBenefit;
import com.firstclub.membership.benefit.dto.EffectiveBenefit;

/**
 * Strategy / Chain-of-Responsibility handler for a single benefit type. Adding a new perk
 * type means adding a new resolver bean; no existing code needs to change (open/closed).
 */
public interface BenefitResolver {

    boolean supports(BenefitType type);

    EffectiveBenefit resolve(TierBenefit tierBenefit);
}
