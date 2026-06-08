package com.firstclub.membership.deal.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Grants a membership plan access to a deal from {@code accessStartAt}.  Setting this earlier
 * than the deal's public start gives that plan early access; premium plans typically receive
 * an earlier timestamp.  Each (deal, plan) pair is unique.
 */
@Entity
@Table(name = "deal_plan_access",
        uniqueConstraints = @UniqueConstraint(name = "uq_deal_plan", columnNames = {"deal_id", "plan_id"}),
        indexes = @Index(name = "idx_deal_plan_access_deal", columnList = "deal_id"))
public class DealPlanAccess extends BaseEntity {

    @Column(name = "deal_id", nullable = false)
    private Long dealId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(nullable = false)
    private Instant accessStartAt;

    protected DealPlanAccess() {
    }

    public DealPlanAccess(Long dealId, Long planId, Instant accessStartAt) {
        this.dealId = dealId;
        this.planId = planId;
        this.accessStartAt = accessStartAt;
    }

    public Long getDealId() {
        return dealId;
    }

    public Long getPlanId() {
        return planId;
    }

    public Instant getAccessStartAt() {
        return accessStartAt;
    }
}
