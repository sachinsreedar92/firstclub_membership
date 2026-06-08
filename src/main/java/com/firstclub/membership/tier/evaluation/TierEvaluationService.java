package com.firstclub.membership.tier.evaluation;

import com.firstclub.membership.tier.domain.Tier;
import java.util.Optional;

/**
 * Configurable tier engine. For a given context it finds every active rule whose applicable
 * criteria are all satisfied, then selects the highest-level tier among them (best qualifying
 * tier). Rules and criteria are data/bean driven, so progression logic changes without code edits.
 *
 * <p>Wrapped in a circuit breaker so a misbehaving rule set degrades gracefully instead of
 * stalling the order-event pipeline.
 */

public interface TierEvaluationService {

    Optional<Tier> evaluateBestTier(TierEvaluationContext context);
}
