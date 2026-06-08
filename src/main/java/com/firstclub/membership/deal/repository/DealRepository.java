package com.firstclub.membership.deal.repository;

import com.firstclub.membership.deal.domain.Deal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findByActiveTrue();

    Optional<Deal> findByCode(String code);
}
