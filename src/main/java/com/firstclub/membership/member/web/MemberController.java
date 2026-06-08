package com.firstclub.membership.member.web;

import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import com.firstclub.membership.member.dto.MembershipView;
import com.firstclub.membership.member.dto.TierProgressionEvent;
import com.firstclub.membership.member.service.MembershipFacade;
import com.firstclub.membership.subscription.domain.SubscriptionHistory;
import com.firstclub.membership.subscription.service.SubscriptionService;
import com.firstclub.membership.tier.service.TierService;
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
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "Member membership view, effective benefits, and tier progression history")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    private final MembershipFacade membershipFacade;
    private final SubscriptionService subscriptionService;
    private final TierService tierService;

    public MemberController(MembershipFacade membershipFacade,
                            SubscriptionService subscriptionService,
                            TierService tierService) {
        this.membershipFacade = membershipFacade;
        this.subscriptionService = subscriptionService;
        this.tierService = tierService;
    }

    @GetMapping("/{userId}/membership")
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "Get a member's current membership, plan, tier and benefits")
    public MembershipView membership(@PathVariable String userId) {
        log.debug("GET /api/v1/members/{}/membership - Getting membership for user: {}", userId, userId);
        return membershipFacade.getMembership(userId);
    }

    @GetMapping("/{userId}/benefits")
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "Get a member's effective benefits for their current tier")
    public List<EffectiveBenefit> benefits(@PathVariable String userId) {
        log.debug("GET /api/v1/members/{}/benefits - Getting benefits for user: {}", userId, userId);
        List<EffectiveBenefit> benefits = membershipFacade.getMemberBenefits(userId);
        log.debug("Found {} benefits for user: {}", benefits.size(), userId);
        return benefits;
    }

    @GetMapping("/{userId}/tier-history")
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "List a member's tier progression events (upgrades, downgrades, auto-changes)")
    public List<TierProgressionEvent> tierHistory(@PathVariable String userId) {
        log.debug("GET /api/v1/members/{}/tier-history - Getting tier progression history for user: {}", userId, userId);
        List<TierProgressionEvent> history = subscriptionService.getTierProgressionHistory(userId).stream()
                .map(h -> toProgressionEvent(h))
                .toList();
        log.debug("Found {} tier progression events for user: {}", history.size(), userId);
        return history;
    }

    private TierProgressionEvent toProgressionEvent(SubscriptionHistory h) {
        String fromCode = h.getFromTierId() == null ? null
                : tierService.findTierCode(h.getFromTierId());
        String toCode = h.getToTierId() == null ? null
                : tierService.findTierCode(h.getToTierId());
        return new TierProgressionEvent(
                h.getId(), h.getAction(),
                h.getFromTierId(), fromCode,
                h.getToTierId(), toCode,
                h.getNote(),
                h.getCreatedAt());
    }
}
