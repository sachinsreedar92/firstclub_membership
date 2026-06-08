package com.firstclub.membership.benefit.web;

import com.firstclub.membership.benefit.dto.BenefitDefinitionRequest;
import com.firstclub.membership.benefit.dto.BenefitDefinitionResponse;
import com.firstclub.membership.benefit.dto.TierBenefitRequest;
import com.firstclub.membership.benefit.dto.TierBenefitResponse;
import com.firstclub.membership.benefit.service.BenefitService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Benefits", description = "Benefit catalog and per-tier perk configuration")
public class BenefitAdminController {

    private static final Logger log = LoggerFactory.getLogger(BenefitAdminController.class);

    private final BenefitService benefitService;

    public BenefitAdminController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }

    @GetMapping("/benefits")
    @Operation(summary = "List benefit definitions")
    public List<BenefitDefinitionResponse> listDefinitions() {
        log.debug("GET /api/v1/benefits - Listing all benefit definitions");
        List<BenefitDefinitionResponse> definitions = benefitService.listDefinitions();
        log.debug("Returned {} benefit definitions", definitions.size());
        return definitions;
    }

    @PostMapping("/benefits")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a benefit definition")
    public BenefitDefinitionResponse createDefinition(
            @Valid @RequestBody BenefitDefinitionRequest request) {
        log.info("POST /api/v1/benefits - Creating new benefit definition: {}", request.code());
        BenefitDefinitionResponse response = benefitService.createDefinition(request);
        log.info("Benefit definition created successfully - id: {}", response.id());
        return response;
    }

    @PostMapping("/tier-benefits")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign a benefit (with configurable value) to a tier")
    public TierBenefitResponse assign(@Valid @RequestBody TierBenefitRequest request) {
        log.info("POST /api/v1/tier-benefits - Assigning benefit {} to tier {}",
                 request.benefitDefinitionId(), request.tierId());
        TierBenefitResponse response = benefitService.assignBenefitToTier(request);
        log.info("Benefit assigned to tier successfully - id: {}", response.id());
        return response;
    }
}
