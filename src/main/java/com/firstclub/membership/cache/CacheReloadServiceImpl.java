package com.firstclub.membership.cache;

import com.firstclub.membership.benefit.service.BenefitService;
import com.firstclub.membership.common.cache.CacheNames;
import com.firstclub.membership.common.exception.BadRequestException;
import com.firstclub.membership.plan.service.MembershipPlanService;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.service.TierService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Operational support for refreshing configuration caches without a restart. A reload evicts the
 * cache and then re-populates it ("warms") by invoking the owning service, so the next request path
 * stays fast. Useful after bulk data changes to plans/tiers/benefits/rules.
 */
@Service
public class CacheReloadServiceImpl implements CacheReloadService {

    private static final Logger log = LoggerFactory.getLogger(CacheReloadService.class);

    private final CacheManager cacheManager;
    private final MembershipPlanService planService;
    private final TierService tierService;
    private final BenefitService benefitService;

    public CacheReloadServiceImpl(CacheManager cacheManager,
                              MembershipPlanService planService,
                              TierService tierService,
                              BenefitService benefitService) {
        this.cacheManager = cacheManager;
        this.planService = planService;
        this.tierService = tierService;
        this.benefitService = benefitService;
    }

    public List<String> cacheNames() {
        return CacheNames.ALL;
    }

    public void reload(String name) {
        if (!CacheNames.ALL.contains(name)) {
            throw new BadRequestException("Unknown cache: " + name
                    + ". Known caches: " + CacheNames.ALL);
        }
        evict(name);
        warm(name);
        log.info("Reloaded cache '{}'", name);
    }

    public void reloadAll() {
        CacheNames.ALL.forEach(name -> {
            evict(name);
            warm(name);
        });
        log.info("Reloaded all caches {}", CacheNames.ALL);
    }

    private void evict(String name) {
        Cache cache = cacheManager.getCache(name);
        if (cache != null) {
            cache.clear();
        }
    }

    private void warm(String name) {
        switch (name) {
            case CacheNames.PLANS -> planService.listActivePlans();
            case CacheNames.TIERS -> tierService.listActiveTiers();
            case CacheNames.BENEFIT_DEFINITIONS -> benefitService.listDefinitions();
            case CacheNames.TIER_RULES -> tierService.activeRules();
            case CacheNames.TIER_BENEFITS -> tierService.listActiveTiers().stream()
                    .map(TierResponse::id)
                    .forEach(benefitService::effectiveBenefitsForTier);
            // Per-key caches (eligibilities, deals access) are repopulated lazily on next read;
            // eviction alone is sufficient and avoids scanning every key here.
            default -> { /* no eager warmer registered */ }
        }
    }
}
