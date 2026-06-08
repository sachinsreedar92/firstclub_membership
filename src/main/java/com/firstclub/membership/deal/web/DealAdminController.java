package com.firstclub.membership.deal.web;

import com.firstclub.membership.deal.domain.Deal;
import com.firstclub.membership.deal.domain.DealPlanAccess;
import com.firstclub.membership.deal.dto.DealRequest;
import com.firstclub.membership.deal.dto.DealTierAccessRequest;
import com.firstclub.membership.deal.dto.PublicDealView;
import com.firstclub.membership.deal.service.DealService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deals")
@Tag(name = "Deals", description = "Exclusive deals and early access to sales: create deals, grant plan access, and view member deals")
public class DealAdminController {

    private static final Logger log = LoggerFactory.getLogger(DealAdminController.class);

    private final DealService dealService;

    public DealAdminController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    @Operation(summary = "List all deals with per-plan access schedule (for product listing / promotions page)")
    public List<PublicDealView> listAll() {
        log.debug("GET /api/v1/deals - Listing all deals");
        List<PublicDealView> deals = dealService.listAllDeals();
        log.debug("Returned {} deals", deals.size());
        return deals;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an exclusive deal or a sale")
    public Deal create(@Valid @RequestBody DealRequest request) {
        log.info("POST /api/v1/deals - Creating new deal: {}", request.code());
        Deal deal = dealService.createDeal(request);
        log.info("Deal created successfully - id: {}, code: {}", deal.getId(), deal.getCode());
        return deal;
    }

    @PostMapping("/{dealId}/plan-access")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Grant a membership plan access to a deal (earlier start = early access)")
    public DealPlanAccess grantAccess(@PathVariable Long dealId,
                                      @Valid @RequestBody DealTierAccessRequest request) {
        log.info("POST /api/v1/deals/{}/plan-access - Granting plan {} access to deal {}",
                 dealId, request.planId(), dealId);
        DealPlanAccess access = dealService.grantPlanAccess(dealId, request);
        log.info("Plan access granted successfully - dealId: {}, planId: {}", dealId, request.planId());
        return access;
    }
}
