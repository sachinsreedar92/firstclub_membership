package com.firstclub.membership.discount.service;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.dto.BenefitEligibilityRequest;
import com.firstclub.membership.discount.dto.BenefitEligibilityResponse;
import java.util.List;

/**
 * Owns scoped discount eligibility rows, keyed by membership plan.  Lookups are cached because
 * discount resolution runs on the checkout hot path; any write evicts the cache immediately.
 */

public interface BenefitEligibilityService {

    List<BenefitEligibility> forPlan(Long planId);
    List<BenefitEligibilityResponse> listAll();
    List<BenefitEligibilityResponse> listByPlan(Long planId);
    BenefitEligibilityResponse create(BenefitEligibilityRequest request);
}
