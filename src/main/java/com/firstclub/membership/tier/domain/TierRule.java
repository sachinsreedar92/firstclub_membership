package com.firstclub.membership.tier.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * A configurable rule that qualifies a member for a {@code targetTier}. All criteria
 * are optional and combined with AND semantics by the evaluation engine. Adding a new
 * threshold means inserting a row; richer criteria types plug in as new Specifications.
 */
@Entity
@Table(name = "tier_rule")
public class TierRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_tier_id", nullable = false)
    private Tier targetTier;

    /** Minimum lifetime/rolling order count required. */
    @Column
    private Integer minOrders;

    /** Minimum order value accumulated in the current calendar month. */
    @Column(precision = 14, scale = 2)
    private BigDecimal minMonthlyOrderValue;

    /** Optional cohort the member must belong to (e.g., EMPLOYEE, VIP). */
    @Column(length = 64)
    private String cohort;

    /** Higher priority rules are evaluated first when multiple tiers qualify. */
    @Column(nullable = false)
    private int priority = 0;

    @Column(nullable = false)
    private boolean active = true;

    protected TierRule() {
    }

    public TierRule(Tier targetTier, Integer minOrders, BigDecimal minMonthlyOrderValue,
                    String cohort, int priority, boolean active) {
        this.targetTier = targetTier;
        this.minOrders = minOrders;
        this.minMonthlyOrderValue = minMonthlyOrderValue;
        this.cohort = cohort;
        this.priority = priority;
        this.active = active;
    }

    public Tier getTargetTier() {
        return targetTier;
    }

    public void setTargetTier(Tier targetTier) {
        this.targetTier = targetTier;
    }

    public Integer getMinOrders() {
        return minOrders;
    }

    public void setMinOrders(Integer minOrders) {
        this.minOrders = minOrders;
    }

    public BigDecimal getMinMonthlyOrderValue() {
        return minMonthlyOrderValue;
    }

    public void setMinMonthlyOrderValue(BigDecimal minMonthlyOrderValue) {
        this.minMonthlyOrderValue = minMonthlyOrderValue;
    }

    public String getCohort() {
        return cohort;
    }

    public void setCohort(String cohort) {
        this.cohort = cohort;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
