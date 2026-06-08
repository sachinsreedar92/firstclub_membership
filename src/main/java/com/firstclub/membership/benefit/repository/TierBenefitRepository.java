package com.firstclub.membership.benefit.repository;

import com.firstclub.membership.benefit.domain.BenefitType;
import com.firstclub.membership.benefit.domain.TierBenefit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface TierBenefitRepository extends JpaRepository<TierBenefit, Long> {

    @EntityGraph(attributePaths = {"benefitDefinition", "tier"})
    List<TierBenefit> findByTierIdAndActiveTrue(Long tierId);

    @EntityGraph(attributePaths = {"benefitDefinition", "tier"})
    List<TierBenefit> findByActiveTrue();

    @EntityGraph(attributePaths = {"benefitDefinition", "tier"})
    Optional<TierBenefit> findFirstByTierIdAndBenefitDefinition_TypeAndActiveTrue(
            Long tierId, BenefitType type);
}
