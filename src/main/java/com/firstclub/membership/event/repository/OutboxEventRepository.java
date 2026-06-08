package com.firstclub.membership.event.repository;

import com.firstclub.membership.event.domain.OutboxEvent;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.Status status, Limit limit);
}
