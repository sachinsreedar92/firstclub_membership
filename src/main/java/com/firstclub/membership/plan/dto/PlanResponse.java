package com.firstclub.membership.plan.dto;

import com.firstclub.membership.plan.domain.BillingCycle;
import com.firstclub.membership.plan.domain.MembershipPlan;
import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String code,
        String name,
        BillingCycle billingCycle,
        BigDecimal price,
        String currency,
        int durationDays,
        boolean active) {

    public static PlanResponse from(MembershipPlan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getBillingCycle(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getDurationDays(),
                plan.isActive());
    }
}
