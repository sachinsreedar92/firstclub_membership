package com.firstclub.membership.plan.web;

import com.firstclub.membership.plan.dto.PlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.service.MembershipPlanService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plans", description = "Membership plans: list, create and update Monthly/Quarterly/Yearly plans")
public class PlanController {

    private static final Logger log = LoggerFactory.getLogger(PlanController.class);

    private final MembershipPlanService planService;

    public PlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "List active membership plans")
    public List<PlanResponse> listPlans() {
        log.info("GET /api/v1/plans - Listing all active membership plans");
        List<PlanResponse> plans = planService.listActivePlans();
        log.debug("Returned {} plans", plans.size());
        return plans;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a membership plan")
    public PlanResponse create(@Valid @RequestBody PlanRequest request) {
        log.info("POST /api/v1/plans - Creating new membership plan: {}", request.code());
        PlanResponse response = planService.create(request);
        log.info("Successfully created plan with id: {}", response.id());
        return response;
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Update a membership plan")
    public PlanResponse update(@PathVariable Long planId, @Valid @RequestBody PlanRequest request) {
        log.info("PUT /api/v1/plans/{} - Updating membership plan: {}", planId, request.code());
        PlanResponse response = planService.update(planId, request);
        log.info("Successfully updated plan with id: {}", planId);
        return response;
    }
}
