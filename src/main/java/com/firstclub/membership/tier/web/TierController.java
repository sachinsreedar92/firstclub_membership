package com.firstclub.membership.tier.web;

import com.firstclub.membership.tier.dto.TierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.TierRuleRequest;
import com.firstclub.membership.tier.dto.TierRuleResponse;
import com.firstclub.membership.tier.service.TierService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tiers")
@Tag(name = "Tiers", description = "Membership tiers: list, create tiers and manage configurable progression rules")
public class TierController {

    private static final Logger log = LoggerFactory.getLogger(TierController.class);

    private final TierService tierService;

    public TierController(TierService tierService) {
        this.tierService = tierService;
    }

    @GetMapping
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "List active tiers with their resolved benefits")
    public List<TierResponse> listTiers() {
        log.info("GET /api/v1/tiers - Listing all active tiers");
        List<TierResponse> tiers = tierService.listActiveTiers();
        log.debug("Returned {} tiers", tiers.size());
        return tiers;
    }

    @GetMapping("/{tierId}")
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "Get a single tier with its benefits")
    public TierResponse getTier(@PathVariable Long tierId) {
        log.debug("GET /api/v1/tiers/{} - Getting tier details", tierId);
        return tierService.getTier(tierId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a tier")
    public TierResponse createTier(@Valid @RequestBody TierRequest request) {
        log.info("POST /api/v1/tiers - Creating new tier: {}", request.code());
        TierResponse response = tierService.createTier(request);
        log.info("Tag created successfully - id: {}", response.id());
        return response;
    }

    @GetMapping("/rules")
    @Operation(summary = "List tier progression rules")
    public List<TierRuleResponse> listRules() {
        log.debug("GET /api/v1/tiers/rules - Listing tier progression rules");
        List<TierRuleResponse> rules = tierService.listRules();
        log.debug("Returned {} tier rules", rules.size());
        return rules;
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a configurable tier progression rule")
    public TierRuleResponse createRule(@Valid @RequestBody TierRuleRequest request) {
        log.info("POST /api/v1/tiers/rules - Creating new tier rule for target tier: {}", request.targetTierId());
        TierRuleResponse response = tierService.createRule(request);
        log.info("Tier rule created successfully - id: {}", response.id());
        return response;
    }
}
