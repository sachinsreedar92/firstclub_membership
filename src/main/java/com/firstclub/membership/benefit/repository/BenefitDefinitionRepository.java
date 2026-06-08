package com.firstclub.membership.benefit.repository;

import com.firstclub.membership.benefit.domain.BenefitDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitDefinitionRepository extends JpaRepository<BenefitDefinition, Long> {

    List<BenefitDefinition> findByActiveTrue();

    Optional<BenefitDefinition> findByCode(String code);
}
