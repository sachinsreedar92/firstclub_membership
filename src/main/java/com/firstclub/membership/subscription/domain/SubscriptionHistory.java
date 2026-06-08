package com.firstclub.membership.subscription.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Immutable audit trail of every change applied to a subscription. */
@Entity
@Table(name = "subscription_history")
public class SubscriptionHistory extends BaseEntity {

    @Column(nullable = false)
    private Long subscriptionId;

    @Column(nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SubscriptionAction action;

    @Column
    private Long fromTierId;

    @Column
    private Long toTierId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private SubscriptionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private SubscriptionStatus toStatus;

    @Column(length = 256)
    private String note;

    protected SubscriptionHistory() {
    }

    private SubscriptionHistory(Builder b) {
        this.subscriptionId = b.subscriptionId;
        this.userId = b.userId;
        this.action = b.action;
        this.fromTierId = b.fromTierId;
        this.toTierId = b.toTierId;
        this.fromStatus = b.fromStatus;
        this.toStatus = b.toStatus;
        this.note = b.note;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public String getUserId() {
        return userId;
    }

    public SubscriptionAction getAction() {
        return action;
    }

    public Long getFromTierId() {
        return fromTierId;
    }

    public Long getToTierId() {
        return toTierId;
    }

    public SubscriptionStatus getFromStatus() {
        return fromStatus;
    }

    public SubscriptionStatus getToStatus() {
        return toStatus;
    }

    public String getNote() {
        return note;
    }

    public static final class Builder {
        private Long subscriptionId;
        private String userId;
        private SubscriptionAction action;
        private Long fromTierId;
        private Long toTierId;
        private SubscriptionStatus fromStatus;
        private SubscriptionStatus toStatus;
        private String note;

        public Builder subscriptionId(Long subscriptionId) {
            this.subscriptionId = subscriptionId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder action(SubscriptionAction action) {
            this.action = action;
            return this;
        }

        public Builder fromTierId(Long fromTierId) {
            this.fromTierId = fromTierId;
            return this;
        }

        public Builder toTierId(Long toTierId) {
            this.toTierId = toTierId;
            return this;
        }

        public Builder fromStatus(SubscriptionStatus fromStatus) {
            this.fromStatus = fromStatus;
            return this;
        }

        public Builder toStatus(SubscriptionStatus toStatus) {
            this.toStatus = toStatus;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public SubscriptionHistory build() {
            return new SubscriptionHistory(this);
        }
    }
}
