package com.firstclub.membership.subscription.repository;

import com.firstclub.membership.subscription.domain.Subscription;
import com.firstclub.membership.subscription.domain.SubscriptionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

    List<Subscription> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Subscription> findByStatusAndExpiryDateBefore(SubscriptionStatus status, Instant cutoff);
}
