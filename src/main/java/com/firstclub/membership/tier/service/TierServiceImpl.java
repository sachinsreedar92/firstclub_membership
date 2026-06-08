package com.firstclub.membership.tier.service;

import com.firstclub.membership.benefit.service.BenefitService;
import com.firstclub.membership.common.cache.CacheNames;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.domain.TierRule;
import com.firstclub.membership.tier.dto.TierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.TierRuleRequest;
import com.firstclub.membership.tier.dto.TierRuleResponse;
import com.firstclub.membership.tier.repository.TierRepository;
import com.firstclub.membership.tier.repository.TierRuleRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catalog and configuration service for tiers and their progression rules. Tier lists
 * (with resolved benefits) and active rule sets are cached, since they are read on hot paths
 * (tier listing, tier evaluation) and only change via admin writes.
 */
@Service
public class TierServiceImpl implements TierService {

    private static final Logger log = LoggerFactory.getLogger(TierServiceImpl.class);

    private final TierRepository tierRepository;
    private final TierRuleRepository tierRuleRepository;
    private final BenefitService benefitService;

    public TierServiceImpl(TierRepository tierRepository,
                       TierRuleRepository tierRuleRepository,
                       BenefitService benefitService) {
        this.tierRepository = tierRepository;
        this.tierRuleRepository = tierRuleRepository;
        this.benefitService = benefitService;
    }

    @Cacheable(cacheNames = CacheNames.TIERS, key = "'active'")
    @Transactional(readOnly = true)
    public List<TierResponse> listActiveTiers() {
        log.debug("Listing active tiers");
        List<TierResponse> tiers = tierRepository.findByActiveTrueOrderByLevelAsc().stream()
                .map(tier -> TierResponse.from(
                        tier, benefitService.effectiveBenefitsForTier(tier.getId())))
                .toList();
        log.debug("Found {} active tiers", tiers.size());
        return tiers;
    }

    @Transactional(readOnly = true)
    public Tier requireTier(Long tierId) {
        log.debug("Retrieving tier with id: {}", tierId);
        return tierRepository.findById(tierId)
                .orElseThrow(() -> {
                    log.warn("Tier not found: {}", tierId);
                    return ResourceNotFoundException.of("Tier", tierId);
                });
    }

    /** Returns the tier code for a given ID, or null if the tier no longer exists. */
    @Transactional(readOnly = true)
    public String findTierCode(Long tierId) {
        if (tierId == null) return null;
        log.debug("Looking up tier code for tier id: {}", tierId);
        return tierRepository.findById(tierId).map(Tier::getCode).orElse("(deleted)");
    }

    @Transactional(readOnly = true)
    public TierResponse getTier(Long tierId) {
        log.debug("Getting tier response for tier id: {}", tierId);
        Tier tier = requireTier(tierId);
        return TierResponse.from(tier, benefitService.effectiveBenefitsForTier(tierId));
    }

    @CacheEvict(cacheNames = CacheNames.TIERS, allEntries = true)
    @Transactional
    public TierResponse createTier(TierRequest request) {
        log.info("Creating new tier with code: {}", request.code());
        tierRepository.findByCode(request.code()).ifPresent(t -> {
            log.warn("Tier code already exists: {}", request.code());
            throw new ConflictException("Tier code already exists: " + request.code());
        });
        Tier tier = new Tier(
                request.code(), request.name(), request.level(),
                request.active() == null || request.active());
        Tier saved = tierRepository.save(tier);
        log.info("Tier created successfully - id={}, code={}, name={}", saved.getId(), saved.getCode(), saved.getName());
        return TierResponse.from(saved, List.of());
    }

    // ----- Tier rules (criteria) -----

    @Cacheable(cacheNames = CacheNames.TIER_RULES, key = "'active'")
    @Transactional(readOnly = true)
    public List<TierRule> activeRules() {
        log.debug("Retrieving active tier rules");
        List<TierRule> rules = tierRuleRepository.findByActiveTrue();
        log.debug("Found {} active tier rules", rules.size());
        return rules;
    }

    @Transactional(readOnly = true)
    public List<TierRuleResponse> listRules() {
        log.debug("Listing all tier rules");
        List<TierRuleResponse> rules = tierRuleRepository.findAll().stream()
                .map(TierRuleResponse::from)
                .toList();
        log.debug("Found {} tier rules", rules.size());
        return rules;
    }

    @CacheEvict(cacheNames = CacheNames.TIER_RULES, allEntries = true)
    @Transactional
    public TierRuleResponse createRule(TierRuleRequest request) {
        log.info("Creating new tier rule for target tier id: {}", request.targetTierId());
        Tier target = requireTier(request.targetTierId());
        TierRule rule = new TierRule(
                target,
                request.minOrders(),
                request.minMonthlyOrderValue(),
                request.cohort(),
                request.priority() == null ? 0 : request.priority(),
                request.active() == null || request.active());
        TierRule saved = tierRuleRepository.save(rule);
        log.info("Tier rule created successfully - id={}, targetTierId={}", saved.getId(), saved.getTargetTier().getId());
        return TierRuleResponse.from(saved);
    }
}
