package com.firstclub.membership.tier.evaluation;

import com.firstclub.membership.tier.domain.TierRule;

/**
 * Specification for a single tier criterion. A criterion only participates when the rule actually
 * configures it ({@link #appliesTo}); the engine then requires all applicable criteria to hold
 * (AND composition). New criterion types are added as new beans without touching the engine.
 */
public interface RuleCriterion {

    String name();

    boolean appliesTo(TierRule rule);

    boolean isSatisfied(TierRule rule, TierEvaluationContext context);
}
