package com.firstclub.membership.plan.service;

import com.firstclub.membership.common.cache.CacheNames;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.dto.PlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.repository.MembershipPlanRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-heavy catalog service for membership plans. List reads are cached in Caffeine;
 * any write evicts the cache so subsequent reads reflect the change.
 */
@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private static final Logger log = LoggerFactory.getLogger(MembershipPlanServiceImpl.class);

    private final MembershipPlanRepository repository;

    public MembershipPlanServiceImpl(MembershipPlanRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = CacheNames.PLANS, key = "'active'")
    @Transactional(readOnly = true)
    public List<PlanResponse> listActivePlans() {
        log.debug("Listing active membership plans");
        List<PlanResponse> plans = repository.findByActiveTrue().stream()
                .map(PlanResponse::from)
                .toList();
        log.debug("Found {} active plans", plans.size());
        return plans;
    }

    @Transactional(readOnly = true)
    public MembershipPlan requireActivePlan(Long planId) {
        log.debug("Retrieving active plan with id: {}", planId);
        MembershipPlan plan = repository.findById(planId)
                .orElseThrow(() -> ResourceNotFoundException.of("Plan", planId));
        if (!plan.isActive()) {
            log.warn("Plan {} is not active", planId);
            throw new ConflictException("Plan is not active: " + planId);
        }
        log.debug("Plan {} retrieved successfully", planId);
        return plan;
    }

    @CacheEvict(cacheNames = CacheNames.PLANS, allEntries = true)
    @Transactional
    public PlanResponse create(PlanRequest request) {
        log.info("Creating new membership plan with code: {}", request.code());
        repository.findByCode(request.code()).ifPresent(p -> {
            log.warn("Plan code already exists: {}", request.code());
            throw new ConflictException("Plan code already exists: " + request.code());
        });
        MembershipPlan plan = MembershipPlan.builder()
                .code(request.code())
                .name(request.name())
                .billingCycle(request.billingCycle())
                .price(request.price())
                .currency(request.currency() == null ? "INR" : request.currency())
                .durationDays(request.durationDays())
                .active(request.active() == null || request.active())
                .build();
        MembershipPlan saved = repository.save(plan);
        log.info("Membership plan created successfully - id={}, code={}, name={}", saved.getId(), saved.getCode(), saved.getName());
        return PlanResponse.from(saved);
    }

    @CacheEvict(cacheNames = CacheNames.PLANS, allEntries = true)
    @Transactional
    public PlanResponse update(Long planId, PlanRequest request) {
        log.info("Updating membership plan - id={}, code={}", planId, request.code());
        MembershipPlan plan = repository.findById(planId)
                .orElseThrow(() -> ResourceNotFoundException.of("Plan", planId));
        plan.setName(request.name());
        plan.setBillingCycle(request.billingCycle());
        plan.setPrice(request.price());
        if (request.currency() != null) {
            plan.setCurrency(request.currency());
        }
        plan.setDurationDays(request.durationDays() > 0
                ? request.durationDays()
                : request.billingCycle().defaultDurationDays());
        if (request.active() != null) {
            plan.setActive(request.active());
        }
        MembershipPlan saved = repository.save(plan);
        log.info("Membership plan updated successfully - id={}, code={}, name={}", saved.getId(), saved.getCode(), saved.getName());
        return PlanResponse.from(saved);
    }
}
