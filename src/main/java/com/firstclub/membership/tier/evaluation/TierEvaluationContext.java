package com.firstclub.membership.tier.evaluation;

import java.math.BigDecimal;

/**
 * Immutable snapshot of the signals tier rules are evaluated against. Adding a new signal here and
 * a matching {@link RuleCriterion} is all that's needed to support a new kind of tier criterion.
 */
public record TierEvaluationContext(
        String userId,
        String cohort,
        long rollingOrderCount,
        BigDecimal monthlyOrderValue) {
}
