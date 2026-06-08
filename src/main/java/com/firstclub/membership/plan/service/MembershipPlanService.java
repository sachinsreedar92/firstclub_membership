package com.firstclub.membership.plan.service;

import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.dto.PlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import java.util.List;

/**
 * Read-heavy catalog service for membership plans. List reads are cached in Caffeine;
 * any write evicts the cache so subsequent reads reflect the change.
 */

public interface MembershipPlanService {

    List<PlanResponse> listActivePlans();
    MembershipPlan requireActivePlan(Long planId);
    PlanResponse create(PlanRequest request);
    PlanResponse update(Long planId, PlanRequest request);
}
