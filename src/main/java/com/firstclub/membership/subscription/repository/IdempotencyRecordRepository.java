package com.firstclub.membership.subscription.repository;

import com.firstclub.membership.subscription.domain.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {
}
