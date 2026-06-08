package com.firstclub.membership.benefit.resolver;

import com.firstclub.membership.benefit.domain.BenefitDefinition;
import com.firstclub.membership.benefit.domain.TierBenefit;
import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Walks the registered {@link BenefitResolver} handlers and delegates to the first one that
 * supports the benefit type. Falls back to a generic representation for unknown types so the
 * system degrades gracefully when a benefit definition has no dedicated resolver yet.
 */
@Component
public class BenefitResolverChain {

    private static final Logger log = LoggerFactory.getLogger(BenefitResolverChain.class);

    private final List<BenefitResolver> resolvers;

    public BenefitResolverChain(List<BenefitResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public EffectiveBenefit resolve(TierBenefit tierBenefit) {
        BenefitDefinition def = tierBenefit.getBenefitDefinition();
        log.debug("Resolving benefit: {} (type: {})", def.getCode(), def.getType());
        return resolvers.stream()
                .filter(r -> r.supports(def.getType()))
                .findFirst()
                .map(r -> {
                    log.debug("Benefit {} resolved by {}", def.getCode(), r.getClass().getSimpleName());
                    return r.resolve(tierBenefit);
                })
                .orElseGet(() -> {
                    log.debug("No specific resolver found for benefit {} (type: {}), using generic representation",
                             def.getCode(), def.getType());
                    return new EffectiveBenefit(
                            def.getType(),
                            def.getCode(),
                            def.getDescription(),
                            tierBenefit.getNumericValue(),
                            def.getDescription());
                });
    }
}
