package com.firstclub.membership.discount.service;

import com.firstclub.membership.common.cache.CacheNames;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.dto.BenefitEligibilityRequest;
import com.firstclub.membership.discount.dto.BenefitEligibilityResponse;
import com.firstclub.membership.discount.repository.BenefitEligibilityRepository;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.repository.MembershipPlanRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns scoped discount eligibility rows, keyed by membership plan.  Lookups are cached because
 * discount resolution runs on the checkout hot path; any write evicts the cache immediately.
 */
@Service
public class BenefitEligibilityServiceImpl implements BenefitEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(BenefitEligibilityServiceImpl.class);

    private final BenefitEligibilityRepository eligibilityRepository;
    private final MembershipPlanRepository planRepository;

    public BenefitEligibilityServiceImpl(BenefitEligibilityRepository eligibilityRepository,
                                     MembershipPlanRepository planRepository) {
        this.eligibilityRepository = eligibilityRepository;
        this.planRepository = planRepository;
    }

    @Cacheable(cacheNames = CacheNames.BENEFIT_ELIGIBILITIES, key = "#planId")
    @Transactional(readOnly = true)
    public List<BenefitEligibility> forPlan(Long planId) {
        log.debug("Retrieving benefit eligibilities for plan id: {}", planId);
        List<BenefitEligibility> eligibilities = eligibilityRepository.findByMembershipPlanIdAndActiveTrue(planId);
        log.debug("Found {} active eligibilities for plan {}", eligibilities.size(), planId);
        return eligibilities;
    }

    @Transactional(readOnly = true)
    public List<BenefitEligibilityResponse> listAll() {
        log.debug("Listing all benefit eligibilities");
        List<BenefitEligibilityResponse> eligibilities = eligibilityRepository.findAll().stream()
                .map(BenefitEligibilityResponse::from)
                .toList();
        log.debug("Found {} benefit eligibilities in total", eligibilities.size());
        return eligibilities;
    }

    @Transactional(readOnly = true)
    public List<BenefitEligibilityResponse> listByPlan(Long planId) {
        log.debug("Listing benefit eligibilities for plan id: {}", planId);
        List<BenefitEligibilityResponse> eligibilities = eligibilityRepository.findByMembershipPlanIdAndActiveTrue(planId).stream()
                .map(BenefitEligibilityResponse::from)
                .toList();
        log.debug("Found {} eligibilities for plan {}", eligibilities.size(), planId);
        return eligibilities;
    }

    @CacheEvict(cacheNames = CacheNames.BENEFIT_ELIGIBILITIES, allEntries = true)
    @Transactional
    public BenefitEligibilityResponse create(BenefitEligibilityRequest request) {
        log.info("Creating new benefit eligibility - planId: {}, scopeType: {}, scopeValue: {}, discountPct: {}",
                 request.planId(), request.scopeType(), request.scopeValue(), request.discountPct());
        MembershipPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> {
                    log.warn("Membership plan not found: {}", request.planId());
                    return ResourceNotFoundException.of("MembershipPlan", request.planId());
                });
        BenefitEligibility eligibility = new BenefitEligibility(
                plan,
                request.scopeType(),
                request.scopeValue(),
                request.discountPct(),
                request.active() == null || request.active());
        BenefitEligibility saved = eligibilityRepository.save(eligibility);
        log.info("Benefit eligibility created successfully - id: {}, planId: {}", saved.getId(), plan.getId());
        return BenefitEligibilityResponse.from(saved);
    }
}
