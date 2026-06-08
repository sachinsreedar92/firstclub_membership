package com.firstclub.membership.tier.evaluation;

import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.domain.TierRule;
import com.firstclub.membership.tier.service.TierService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Configurable tier engine. For a given context it finds every active rule whose applicable
 * criteria are all satisfied, then selects the highest-level tier among them (best qualifying
 * tier). Rules and criteria are data/bean driven, so progression logic changes without code edits.
 *
 * <p>Wrapped in a circuit breaker so a misbehaving rule set degrades gracefully instead of
 * stalling the order-event pipeline.
 */
@Service
public class TierEvaluationServiceImpl implements TierEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(TierEvaluationService.class);

    private final TierService tierService;
    private final List<RuleCriterion> criteria;

    public TierEvaluationServiceImpl(TierService tierService, List<RuleCriterion> criteria) {
        this.tierService = tierService;
        this.criteria = criteria;
    }

    @CircuitBreaker(name = "tierEvaluation")
    public Optional<Tier> evaluateBestTier(TierEvaluationContext context) {
        log.debug("Evaluating best tier for user: {}", context.userId());
        List<TierRule> rules = tierService.activeRules();
        log.debug("Evaluating against {} active tier rules", rules.size());
        Optional<Tier> bestTier = rules.stream()
                .filter(rule -> qualifies(rule, context))
                .map(TierRule::getTargetTier)
                .filter(Tier::isActive)
                .max(Comparator.comparingInt(Tier::getLevel)
                        .thenComparing(Tier::getId));
        if (bestTier.isPresent()) {
            log.info("User {} qualified for tier: {}", context.userId(), bestTier.get().getCode());
        } else {
            log.debug("User {} did not qualify for any tier", context.userId());
        }
        return bestTier;
    }

    private boolean qualifies(TierRule rule, TierEvaluationContext context) {
        boolean anyApplicable = false;
        for (RuleCriterion criterion : criteria) {
            if (!criterion.appliesTo(rule)) {
                continue;
            }
            anyApplicable = true;
            if (!criterion.isSatisfied(rule, context)) {
                return false;
            }
        }
        // A rule with no configured criteria never auto-qualifies a member.
        boolean qualifies = anyApplicable;
        if (qualifies) {
            log.debug("User {} qualifies for tier {} via rule {}",
                    context.userId(), rule.getTargetTier().getCode(), rule.getId());
        }
        return qualifies;
    }
}
