package com.firstclub.membership.subscription.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * A user's membership subscription. Concurrency safety is provided by:
 * <ul>
 *   <li>{@code @Version} optimistic locking for safe concurrent upgrade/downgrade/cancel.</li>
 *   <li>{@code uniqueActiveKey} which equals the userId while ACTIVE and is null otherwise,
 *       backed by a unique constraint so a user can hold at most one ACTIVE subscription.</li>
 * </ul>
 */
@Entity
@Table(name = "subscription", uniqueConstraints =
        @UniqueConstraint(name = "uq_active_subscription_per_user", columnNames = "unique_active_key"))
public class Subscription extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false)
    private Long planId;

    @Column(nullable = false)
    private Long tierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean autoRenew;

    /** Mirror of userId while ACTIVE; null once cancelled/expired (see class doc). */
    @Column(name = "unique_active_key", length = 64)
    private String uniqueActiveKey;

    @Version
    private long version;

    protected Subscription() {
    }

    private Subscription(Builder builder) {
        this.userId = builder.userId;
        this.planId = builder.planId;
        this.tierId = builder.tierId;
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = builder.startDate;
        this.expiryDate = builder.expiryDate;
        this.autoRenew = builder.autoRenew;
        this.uniqueActiveKey = builder.userId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void changeTier(Long newTierId) {
        this.tierId = newTierId;
    }

    public void deactivate(SubscriptionStatus newStatus) {
        this.status = newStatus;
        this.uniqueActiveKey = null;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public String getUserId() {
        return userId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getTierId() {
        return tierId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public long getVersion() {
        return version;
    }

    public static final class Builder {
        private String userId;
        private Long planId;
        private Long tierId;
        private Instant startDate = Instant.now();
        private Instant expiryDate;
        private boolean autoRenew = true;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder planId(Long planId) {
            this.planId = planId;
            return this;
        }

        public Builder tierId(Long tierId) {
            this.tierId = tierId;
            return this;
        }

        public Builder startDate(Instant startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder expiryDate(Instant expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder autoRenew(boolean autoRenew) {
            this.autoRenew = autoRenew;
            return this;
        }

        public Subscription build() {
            return new Subscription(this);
        }
    }
}
