package com.firstclub.membership.discount.repository;

import com.firstclub.membership.discount.domain.BenefitEligibility;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitEligibilityRepository extends JpaRepository<BenefitEligibility, Long> {

    List<BenefitEligibility> findByMembershipPlanIdAndActiveTrue(Long planId);

    boolean existsByMembershipPlanId(Long planId);
}
