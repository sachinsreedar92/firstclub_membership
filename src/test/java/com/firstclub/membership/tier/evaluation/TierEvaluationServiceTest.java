package com.firstclub.membership.tier.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.domain.TierRule;
import com.firstclub.membership.tier.service.TierService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit tests for the configurable tier engine: verifies criteria composition (AND within a
 * rule, OR across rules) and best-tier selection by level.
 */
@ExtendWith(MockitoExtension.class)
class TierEvaluationServiceTest {

    @Mock
    private TierService tierService;

    private TierEvaluationService engine;

    private final Tier silver = tier(1L, "SILVER", 1);
    private final Tier gold = tier(2L, "GOLD", 2);
    private final Tier platinum = tier(3L, "PLATINUM", 3);

    @BeforeEach
    void setUp() {
        List<RuleCriterion> criteria = List.of(
                new MinOrdersCriterion(),
                new MonthlyOrderValueCriterion(),
                new CohortCriterion());
        engine = new TierEvaluationServiceImpl(tierService, criteria);

        when(tierService.activeRules()).thenReturn(List.of(
                new TierRule(gold, 5, null, null, 10, true),
                new TierRule(gold, null, new BigDecimal("5000"), null, 10, true),
                new TierRule(platinum, 15, null, null, 20, true),
                new TierRule(platinum, null, new BigDecimal("20000"), null, 20, true),
                new TierRule(platinum, null, null, "VIP", 30, true)));
    }

    @Test
    void promotesToGoldAfterEnoughOrders() {
        Optional<Tier> result = engine.evaluateBestTier(
                new TierEvaluationContext("u1", null, 6, new BigDecimal("100")));
        assertThat(result).contains(gold);
    }

    @Test
    void promotesToPlatinumOnHighOrderCount() {
        Optional<Tier> result = engine.evaluateBestTier(
                new TierEvaluationContext("u1", null, 16, new BigDecimal("100")));
        assertThat(result).contains(platinum);
    }

    @Test
    void cohortAloneQualifiesForPlatinum() {
        Optional<Tier> result = engine.evaluateBestTier(
                new TierEvaluationContext("u1", "VIP", 1, BigDecimal.ZERO));
        assertThat(result).contains(platinum);
    }

    @Test
    void noQualifyingTierWhenBelowAllThresholds() {
        Optional<Tier> result = engine.evaluateBestTier(
                new TierEvaluationContext("u1", null, 2, new BigDecimal("100")));
        assertThat(result).isEmpty();
    }

    private static Tier tier(Long id, String code, int level) {
        Tier tier = new Tier(code, code, level, true);
        tier.setId(id);
        return tier;
    }
}
