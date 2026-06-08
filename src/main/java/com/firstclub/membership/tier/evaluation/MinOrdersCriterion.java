package com.firstclub.membership.tier.evaluation;

import com.firstclub.membership.tier.domain.TierRule;
import org.springframework.stereotype.Component;

/** Qualifies when the member's order count meets the rule's minimum. */
@Component
public class MinOrdersCriterion implements RuleCriterion {

    @Override
    public String name() {
        return "MIN_ORDERS";
    }

    @Override
    public boolean appliesTo(TierRule rule) {
        return rule.getMinOrders() != null;
    }

    @Override
    public boolean isSatisfied(TierRule rule, TierEvaluationContext context) {
        return context.rollingOrderCount() >= rule.getMinOrders();
    }
}
