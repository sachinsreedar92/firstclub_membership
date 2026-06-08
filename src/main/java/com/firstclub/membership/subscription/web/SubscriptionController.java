package com.firstclub.membership.subscription.web;

import com.firstclub.membership.subscription.dto.ChangeTierRequest;
import com.firstclub.membership.subscription.dto.SubscribeRequest;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import com.firstclub.membership.subscription.service.SubscriptionService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Subscribe, upgrade, downgrade, cancel, and track membership")
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimiter(name = "subscriptionWrite")
    @Bulkhead(name = "subscriptionWrite")
    @Operation(summary = "Subscribe to a plan + tier (idempotent via Idempotency-Key header)")
    public SubscriptionResponse subscribe(
            @Valid @RequestBody SubscribeRequest request,
            @Parameter(description = "Optional idempotency key to safely retry subscribe")
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("POST /api/v1/subscriptions - Subscribing user: {} to plan: {}, tier: {}",
                 request.userId(), request.planId(), request.tierId());
        SubscriptionResponse response = subscriptionService.subscribe(request, idempotencyKey);
        log.info("Subscription created: id={}, user={}", response.id(), request.userId());
        return response;
    }

    @PatchMapping("/{subscriptionId}/upgrade")
    @RateLimiter(name = "subscriptionWrite")
    @Bulkhead(name = "subscriptionWrite")
    @Operation(summary = "Upgrade a subscription to a higher tier")
    public SubscriptionResponse upgrade(@PathVariable Long subscriptionId,
                                        @Valid @RequestBody ChangeTierRequest request) {
        log.info("PATCH /api/v1/subscriptions/{}/upgrade - Upgrading to tier: {}",
                 subscriptionId, request.targetTierId());
        SubscriptionResponse response = subscriptionService.upgrade(subscriptionId, request.targetTierId());
        log.info("Subscription upgraded: id={}", subscriptionId);
        return response;
    }

    @PatchMapping("/{subscriptionId}/downgrade")
    @RateLimiter(name = "subscriptionWrite")
    @Bulkhead(name = "subscriptionWrite")
    @Operation(summary = "Downgrade a subscription to a lower tier")
    public SubscriptionResponse downgrade(@PathVariable Long subscriptionId,
                                          @Valid @RequestBody ChangeTierRequest request) {
        log.info("PATCH /api/v1/subscriptions/{}/downgrade - Downgrading to tier: {}",
                 subscriptionId, request.targetTierId());
        SubscriptionResponse response = subscriptionService.downgrade(subscriptionId, request.targetTierId());
        log.info("Subscription downgraded: id={}", subscriptionId);
        return response;
    }

    @PostMapping("/{subscriptionId}/cancel")
    @RateLimiter(name = "subscriptionWrite")
    @Operation(summary = "Cancel a subscription")
    public SubscriptionResponse cancel(@PathVariable Long subscriptionId) {
        log.info("POST /api/v1/subscriptions/{}/cancel - Cancelling subscription", subscriptionId);
        SubscriptionResponse response = subscriptionService.cancel(subscriptionId);
        log.info("Subscription cancelled: id={}", subscriptionId);
        return response;
    }

    @GetMapping
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "Get the user's current active subscription (membership + expiry)")
    public SubscriptionResponse current(@RequestParam String userId) {
        log.debug("GET /api/v1/subscriptions - Getting current subscription for user: {}", userId);
        return subscriptionService.getCurrentSubscription(userId);
    }

    @GetMapping("/history")
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "List a user's subscriptions (most recent first)")
    public List<SubscriptionResponse> history(@RequestParam String userId) {
        log.debug("GET /api/v1/subscriptions/history - Getting subscription history for user: {}", userId);
        List<SubscriptionResponse> history = subscriptionService.getSubscriptionHistory(userId);
        log.debug("Found {} subscription records for user: {}", history.size(), userId);
        return history;
    }
}
