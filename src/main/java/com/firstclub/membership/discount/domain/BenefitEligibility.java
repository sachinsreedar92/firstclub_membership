package com.firstclub.membership.discount.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import com.firstclub.membership.plan.domain.MembershipPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Scopes a membership plan's extra-discount to a specific product item, product category, or
 * everything (ALL).  Each row carries its own {@code discountPct} so a plan can offer different
 * percentages per item/category (e.g. 20% on a SKU, 12% on a category, 5% across the board).
 * The "most-specific-wins" rule is applied at resolution time via {@link EligibilityScopeType#specificity()}.
 */
@Entity
@Table(name = "benefit_eligibility", indexes = {
        @Index(name = "idx_eligibility_plan", columnList = "plan_id"),
        @Index(name = "idx_eligibility_scope", columnList = "scopeType,scopeValue")
})
public class BenefitEligibility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan membershipPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EligibilityScopeType scopeType;

    /** Product item ID or product category ID; null / ignored for ALL scope. */
    @Column(length = 128)
    private String scopeValue;

    /** Explicit discount percentage for this scope. Must be > 0 and ≤ 100. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPct;

    @Column(nullable = false)
    private boolean active = true;

    protected BenefitEligibility() {
    }

    public BenefitEligibility(MembershipPlan membershipPlan, EligibilityScopeType scopeType,
                              String scopeValue, BigDecimal discountPct, boolean active) {
        this.membershipPlan = membershipPlan;
        this.scopeType = scopeType;
        this.scopeValue = scopeValue;
        this.discountPct = discountPct;
        this.active = active;
    }

    public MembershipPlan getMembershipPlan() {
        return membershipPlan;
    }

    public EligibilityScopeType getScopeType() {
        return scopeType;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public boolean isActive() {
        return active;
    }
}
