package com.firstclub.membership.tier.evaluation;

import com.firstclub.membership.tier.domain.TierRule;
import org.springframework.stereotype.Component;

/** Qualifies when the member belongs to the cohort required by the rule. */
@Component
public class CohortCriterion implements RuleCriterion {

    @Override
    public String name() {
        return "COHORT";
    }

    @Override
    public boolean appliesTo(TierRule rule) {
        return rule.getCohort() != null && !rule.getCohort().isBlank();
    }

    @Override
    public boolean isSatisfied(TierRule rule, TierEvaluationContext context) {
        return rule.getCohort().equalsIgnoreCase(context.cohort());
    }
}
