package com.firstclub.membership.discount.web;

import com.firstclub.membership.discount.dto.BenefitEligibilityRequest;
import com.firstclub.membership.discount.dto.BenefitEligibilityResponse;
import com.firstclub.membership.discount.service.BenefitEligibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/benefit-eligibilities")
@Tag(name = "Discounts", description = "Member discount badges for product listing pages and cart-level resolution")
public class EligibilityAdminController {

    private static final Logger log = LoggerFactory.getLogger(EligibilityAdminController.class);

    private final BenefitEligibilityService eligibilityService;

    public EligibilityAdminController(BenefitEligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @GetMapping
    @Operation(summary = "List all benefit eligibilities (optionally filter by planId)")
    public List<BenefitEligibilityResponse> list(
            @RequestParam(required = false) Long planId) {
        if (planId != null) {
            log.debug("GET /api/v1/benefit-eligibilities - Listing benefit eligibilities for plan: {}", planId);
            return eligibilityService.listByPlan(planId);
        }
        log.debug("GET /api/v1/benefit-eligibilities - Listing all benefit eligibilities");
        return eligibilityService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a scoped discount eligibility to a plan (ALL / PRODUCT_CATEGORY / PRODUCT_ITEM)")
    public BenefitEligibilityResponse create(@Valid @RequestBody BenefitEligibilityRequest request) {
        log.info("POST /api/v1/benefit-eligibilities - Creating eligibility for plan: {}, scope: {}, discount: {}%",
                 request.planId(), request.scopeType(), request.discountPct());
        BenefitEligibilityResponse response = eligibilityService.create(request);
        log.info("Benefit eligibility created successfully - id: {}, planId: {}", response.id(), request.planId());
        return response;
    }
}
