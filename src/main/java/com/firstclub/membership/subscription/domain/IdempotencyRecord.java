package com.firstclub.membership.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Stores the result of a previously processed idempotent subscribe request. The
 * idempotency key is the primary key, so a duplicate request maps to the same row and
 * returns the original subscription instead of creating a second one.
 */
@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecord {

    @Id
    @Column(length = 128)
    private String idempotencyKey;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false)
    private Long subscriptionId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, String userId, Long subscriptionId) {
        this.idempotencyKey = idempotencyKey;
        this.userId = userId;
        this.subscriptionId = subscriptionId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getUserId() {
        return userId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
