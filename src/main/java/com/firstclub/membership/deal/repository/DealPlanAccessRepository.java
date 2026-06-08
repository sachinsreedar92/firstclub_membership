package com.firstclub.membership.deal.repository;

import com.firstclub.membership.deal.domain.DealPlanAccess;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealPlanAccessRepository extends JpaRepository<DealPlanAccess, Long> {

    List<DealPlanAccess> findByDealId(Long dealId);
}
