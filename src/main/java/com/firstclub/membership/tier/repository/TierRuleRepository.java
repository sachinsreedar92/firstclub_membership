package com.firstclub.membership.tier.repository;

import com.firstclub.membership.tier.domain.TierRule;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierRuleRepository extends JpaRepository<TierRule, Long> {

    @EntityGraph(attributePaths = "targetTier")
    List<TierRule> findByActiveTrue();
}
