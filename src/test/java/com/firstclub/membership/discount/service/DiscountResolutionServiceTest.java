package com.firstclub.membership.discount.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import com.firstclub.membership.discount.dto.DiscountContext;
import com.firstclub.membership.discount.dto.DiscountResult;
import com.firstclub.membership.discount.matcher.AllEligibilityMatcher;
import com.firstclub.membership.discount.matcher.CategoryEligibilityMatcher;
import com.firstclub.membership.discount.matcher.ProductEligibilityMatcher;
import com.firstclub.membership.plan.domain.BillingCycle;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.repository.MembershipPlanRepository;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import com.firstclub.membership.subscription.domain.SubscriptionStatus;
import com.firstclub.membership.subscription.service.SubscriptionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for plan-based scoped discount resolution:
 *  - PRODUCT_ITEM scope beats PRODUCT_CATEGORY scope (most-specific-wins)
 *  - Items outside any configured scope receive no discount
 *  - Members without an active subscription receive no discount
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscountResolutionServiceTest {

    @Mock private SubscriptionService subscriptionService;
    @Mock private MembershipPlanRepository planRepository;
    @Mock private BenefitEligibilityService eligibilityService;

    private DiscountResolutionService service;

    private static final Long PLAN_ID = 10L;
    private static final MembershipPlan YEARLY_PLAN = yearlyPlan();

    @BeforeEach
    void setUp() {
        service = new DiscountResolutionServiceImpl(subscriptionService, planRepository,
                eligibilityService, List.of(new AllEligibilityMatcher(),
                new CategoryEligibilityMatcher(), new ProductEligibilityMatcher()));

        SubscriptionResponse activeSub = new SubscriptionResponse(
                1L, "u1", PLAN_ID, 2L, SubscriptionStatus.ACTIVE,
                Instant.now(), Instant.now().plusSeconds(86400), true, 1L);
        when(subscriptionService.findActiveSubscription("u1")).thenReturn(Optional.of(activeSub));
        when(planRepository.findById(PLAN_ID)).thenReturn(Optional.of(YEARLY_PLAN));

        when(eligibilityService.forPlan(eq(PLAN_ID))).thenReturn(List.of(
                eligibility(YEARLY_PLAN, EligibilityScopeType.PRODUCT_CATEGORY, "ELECTRONICS", "10.00"),
                eligibility(YEARLY_PLAN, EligibilityScopeType.PRODUCT_ITEM, "SKU-IPHONE", "25.00")));
    }

    @Test
    void productItemScopeBeatsCategoryScope() {
        DiscountResult result = service.resolve("u1",
                new DiscountContext("SKU-IPHONE", "ELECTRONICS"));
        assertThat(result.eligible()).isTrue();
        assertThat(result.discountPercentage()).isEqualByComparingTo("25");
        assertThat(result.matchedScope()).isEqualTo(EligibilityScopeType.PRODUCT_ITEM);
    }

    @Test
    void categoryScopeAppliesWhenNoProductItemMatch() {
        DiscountResult result = service.resolve("u1",
                new DiscountContext("SKU-OTHER", "ELECTRONICS"));
        assertThat(result.eligible()).isTrue();
        assertThat(result.discountPercentage()).isEqualByComparingTo("10");
        assertThat(result.matchedScope()).isEqualTo(EligibilityScopeType.PRODUCT_CATEGORY);
    }

    @Test
    void noDiscountWhenItemOutsideAllScopes() {
        DiscountResult result = service.resolve("u1",
                new DiscountContext("SKU-OTHER", "GROCERY"));
        assertThat(result.eligible()).isFalse();
        assertThat(result.discountPercentage()).isEqualByComparingTo("0");
    }

    @Test
    void noDiscountWhenNoActiveSubscription() {
        when(subscriptionService.findActiveSubscription("u2")).thenReturn(Optional.empty());
        DiscountResult result = service.resolve("u2", new DiscountContext(null, null));
        assertThat(result.eligible()).isFalse();
        assertThat(result.reason()).contains("No active membership subscription");
    }

    private static MembershipPlan yearlyPlan() {
        MembershipPlan p = MembershipPlan.builder()
                .code("YEARLY").name("Yearly Membership")
                .billingCycle(BillingCycle.YEARLY).price(new BigDecimal("1499.00"))
                .build();
        p.setId(PLAN_ID);
        return p;
    }

    private static BenefitEligibility eligibility(MembershipPlan plan, EligibilityScopeType scope,
                                                   String scopeValue, String pct) {
        return new BenefitEligibility(plan, scope, scopeValue, new BigDecimal(pct), true);
    }
}
