package com.firstclub.membership.benefit.service;

import com.firstclub.membership.benefit.dto.BenefitDefinitionRequest;
import com.firstclub.membership.benefit.dto.BenefitDefinitionResponse;
import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import com.firstclub.membership.benefit.dto.TierBenefitRequest;
import com.firstclub.membership.benefit.dto.TierBenefitResponse;
import java.util.List;

/**
 * Owns benefit definitions and the per-tier benefit configuration, and produces the
 * resolved "effective benefits" view used by tiers/members. Effective-benefit lookups are
 * cached per tier since they are read on every benefit query but change rarely.
 */

public interface BenefitService {

    List<EffectiveBenefit> effectiveBenefitsForTier(Long tierId);
    List<BenefitDefinitionResponse> listDefinitions();
    BenefitDefinitionResponse createDefinition(BenefitDefinitionRequest request);
    TierBenefitResponse assignBenefitToTier(TierBenefitRequest request);
}
