package com.firstclub.membership.member.repository;

import com.firstclub.membership.member.domain.UserOrderStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOrderStatsRepository extends JpaRepository<UserOrderStats, Long> {

    Optional<UserOrderStats> findByUserId(String userId);
}
