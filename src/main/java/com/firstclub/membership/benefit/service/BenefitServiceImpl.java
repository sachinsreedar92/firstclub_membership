package com.firstclub.membership.benefit.service;

import com.firstclub.membership.benefit.domain.BenefitDefinition;
import com.firstclub.membership.benefit.domain.TierBenefit;
import com.firstclub.membership.benefit.dto.BenefitDefinitionRequest;
import com.firstclub.membership.benefit.dto.BenefitDefinitionResponse;
import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import com.firstclub.membership.benefit.dto.TierBenefitRequest;
import com.firstclub.membership.benefit.dto.TierBenefitResponse;
import com.firstclub.membership.benefit.repository.BenefitDefinitionRepository;
import com.firstclub.membership.benefit.repository.TierBenefitRepository;
import com.firstclub.membership.benefit.resolver.BenefitResolverChain;
import com.firstclub.membership.common.cache.CacheNames;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.repository.TierRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns benefit definitions and the per-tier benefit configuration, and produces the
 * resolved "effective benefits" view used by tiers/members. Effective-benefit lookups are
 * cached per tier since they are read on every benefit query but change rarely.
 */
@Service
public class BenefitServiceImpl implements BenefitService {

    private static final Logger log = LoggerFactory.getLogger(BenefitServiceImpl.class);

    private final BenefitDefinitionRepository definitionRepository;
    private final TierBenefitRepository tierBenefitRepository;
    private final TierRepository tierRepository;
    private final BenefitResolverChain resolverChain;

    public BenefitServiceImpl(BenefitDefinitionRepository definitionRepository,
                          TierBenefitRepository tierBenefitRepository,
                          TierRepository tierRepository,
                          BenefitResolverChain resolverChain) {
        this.definitionRepository = definitionRepository;
        this.tierBenefitRepository = tierBenefitRepository;
        this.tierRepository = tierRepository;
        this.resolverChain = resolverChain;
    }

    @Cacheable(cacheNames = CacheNames.TIER_BENEFITS, key = "#tierId")
    @Transactional(readOnly = true)
    public List<EffectiveBenefit> effectiveBenefitsForTier(Long tierId) {
        log.debug("Retrieving effective benefits for tier id: {}", tierId);
        List<EffectiveBenefit> benefits = tierBenefitRepository.findByTierIdAndActiveTrue(tierId).stream()
                .map(resolverChain::resolve)
                .toList();
        log.debug("Found {} effective benefits for tier {}", benefits.size(), tierId);
        return benefits;
    }

    @Cacheable(cacheNames = CacheNames.BENEFIT_DEFINITIONS, key = "'active'")
    @Transactional(readOnly = true)
    public List<BenefitDefinitionResponse> listDefinitions() {
        log.debug("Listing all active benefit definitions");
        List<BenefitDefinitionResponse> definitions = definitionRepository.findByActiveTrue().stream()
                .map(BenefitDefinitionResponse::from)
                .toList();
        log.debug("Found {} active benefit definitions", definitions.size());
        return definitions;
    }

    @CacheEvict(cacheNames = CacheNames.BENEFIT_DEFINITIONS, allEntries = true)
    @Transactional
    public BenefitDefinitionResponse createDefinition(BenefitDefinitionRequest request) {
        log.info("Creating new benefit definition with code: {}", request.code());
        definitionRepository.findByCode(request.code()).ifPresent(d -> {
            log.warn("Benefit code already exists: {}", request.code());
            throw new ConflictException("Benefit code already exists: " + request.code());
        });
        BenefitDefinition def = new BenefitDefinition(
                request.code(),
                request.type(),
                request.description(),
                request.active() == null || request.active());
        BenefitDefinition saved = definitionRepository.save(def);
        log.info("Benefit definition created successfully - id={}, code={}, type={}", saved.getId(), saved.getCode(), saved.getType());
        return BenefitDefinitionResponse.from(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TIER_BENEFITS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TIERS, allEntries = true)
    })
    @Transactional
    public TierBenefitResponse assignBenefitToTier(TierBenefitRequest request) {
        log.info("Assigning benefit id={} to tier id={}", request.benefitDefinitionId(), request.tierId());
        Tier tier = tierRepository.findById(request.tierId())
                .orElseThrow(() -> {
                    log.warn("Tier not found: {}", request.tierId());
                    return ResourceNotFoundException.of("Tier", request.tierId());
                });
        BenefitDefinition def = definitionRepository.findById(request.benefitDefinitionId())
                .orElseThrow(() -> {
                    log.warn("Benefit definition not found: {}", request.benefitDefinitionId());
                    return ResourceNotFoundException.of("BenefitDefinition", request.benefitDefinitionId());
                });
        TierBenefit tierBenefit = new TierBenefit(
                tier,
                def,
                request.numericValue(),
                //request.configJson(),
                null,
                request.active() == null || request.active());
        TierBenefit saved = tierBenefitRepository.save(tierBenefit);
        log.info("Benefit assigned to tier successfully - tierBenefitId={}, tierId={}, benefitId={}",
                 saved.getId(), tier.getId(), def.getId());
        return TierBenefitResponse.from(saved);
    }
}
