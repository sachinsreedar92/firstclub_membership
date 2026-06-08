package com.firstclub.membership.subscription.repository;

import com.firstclub.membership.subscription.domain.SubscriptionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    List<SubscriptionHistory> findBySubscriptionIdOrderByCreatedAtAsc(Long subscriptionId);

    List<SubscriptionHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}
