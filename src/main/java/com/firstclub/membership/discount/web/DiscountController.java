package com.firstclub.membership.discount.web;

import com.firstclub.membership.discount.dto.DiscountContext;
import com.firstclub.membership.discount.dto.DiscountResult;
import com.firstclub.membership.discount.dto.PlanDiscountView;
import com.firstclub.membership.discount.service.DiscountResolutionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/{userId}")
@Tag(name = "Discounts", description = "Member discount badges for product listing pages and cart-level resolution")
public class DiscountController {

    private static final Logger log = LoggerFactory.getLogger(DiscountController.class);

    private final DiscountResolutionService discountResolutionService;

    public DiscountController(DiscountResolutionService discountResolutionService) {
        this.discountResolutionService = discountResolutionService;
    }

    /**
     * <b>Product listing page</b> — returns every discount badge active for the member's plan,
     * grouped by scope (ALL / PRODUCT_CATEGORY / PRODUCT_ITEM).
     *
     * The frontend indexes these once and annotates every product without further API calls:
     * item badges (SKU-level) take priority over category badges, which take priority over
     * plan-wide badges.
     */
    @GetMapping("/discounts")
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "List all plan discount badges for the product listing page (grouped by scope)")
    public PlanDiscountView planDiscounts(@PathVariable String userId) {
        log.debug("GET /api/v1/members/{}/discounts - Getting plan discounts for user: {}", userId, userId);
        return discountResolutionService.listPlanDiscounts(userId);
    }

    /**
     * <b>Cart / checkout validation</b> — resolves the single best-matching discount for one
     * product using most-specific-wins. Pass productId and/or categoryId to narrow the match.
     */
    @GetMapping("/discount")
    @RateLimiter(name = "catalogRead")
    @Operation(summary = "Resolve the exact discount % for a single product (cart validation)")
    public DiscountResult resolve(@PathVariable String userId,
                                  @RequestParam(required = false) String productId,
                                  @RequestParam(required = false) String categoryId) {
        log.debug("GET /api/v1/members/{}/discount - Resolving discount for user: {}, productId: {}, categoryId: {}",
                 userId, userId, productId, categoryId);
        return discountResolutionService.resolve(userId, new DiscountContext(productId, categoryId));
    }
}
