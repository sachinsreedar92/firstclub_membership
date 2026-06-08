package com.firstclub.membership.deal.web;

import com.firstclub.membership.deal.dto.DealResponse;
import com.firstclub.membership.deal.service.DealService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/{userId}/deals")
@Tag(name = "Deals", description = "Exclusive deals and early access to sales: create deals, grant tier access, and view member deals")
public class DealController {

    private static final Logger log = LoggerFactory.getLogger(DealController.class);

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "List deals currently accessible to the member (incl. early access)")
    public List<DealResponse> deals(@PathVariable String userId) {
        log.debug("GET /api/v1/members/{}/deals - Getting accessible deals for user: {}", userId, userId);
        List<DealResponse> deals = dealService.accessibleDeals(userId);
        log.debug("Found {} accessible deals for user: {}", deals.size(), userId);
        return deals;
    }
}
