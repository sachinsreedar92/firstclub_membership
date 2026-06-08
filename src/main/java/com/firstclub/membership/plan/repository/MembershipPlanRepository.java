package com.firstclub.membership.plan.repository;

import com.firstclub.membership.plan.domain.MembershipPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    List<MembershipPlan> findByActiveTrue();

    Optional<MembershipPlan> findByCode(String code);
}
