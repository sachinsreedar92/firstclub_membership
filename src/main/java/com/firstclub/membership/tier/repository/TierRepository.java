package com.firstclub.membership.tier.repository;

import com.firstclub.membership.tier.domain.Tier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierRepository extends JpaRepository<Tier, Long> {

    List<Tier> findByActiveTrueOrderByLevelAsc();

    Optional<Tier> findByCode(String code);
}
