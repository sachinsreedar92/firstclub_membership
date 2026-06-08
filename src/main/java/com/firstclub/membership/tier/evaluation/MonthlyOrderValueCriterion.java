package com.firstclub.membership.tier.evaluation;

import com.firstclub.membership.tier.domain.TierRule;
import org.springframework.stereotype.Component;

/** Qualifies when the member's current-month order value meets the rule's minimum. */
@Component
public class MonthlyOrderValueCriterion implements RuleCriterion {

    @Override
    public String name() {
        return "MONTHLY_ORDER_VALUE";
    }

    @Override
    public boolean appliesTo(TierRule rule) {
        return rule.getMinMonthlyOrderValue() != null;
    }

    @Override
    public boolean isSatisfied(TierRule rule, TierEvaluationContext context) {
        return context.monthlyOrderValue() != null
                && context.monthlyOrderValue().compareTo(rule.getMinMonthlyOrderValue()) >= 0;
    }
}
