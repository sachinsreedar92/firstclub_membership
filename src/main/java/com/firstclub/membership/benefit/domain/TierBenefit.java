package com.firstclub.membership.benefit.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import com.firstclub.membership.tier.domain.Tier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * Binds a {@link BenefitDefinition} to a {@link Tier} with a configurable value
 * (e.g., discount percentage, free-delivery threshold). This is what makes
 * "each tier unlocks additional perks" fully configurable at the data layer.
 */
@Entity
@Table(name = "tier_benefit", uniqueConstraints = @UniqueConstraint(
        name = "uq_tier_benefit", columnNames = {"tier_id", "benefit_definition_id"}))
public class TierBenefit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private Tier tier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "benefit_definition_id", nullable = false)
    private BenefitDefinition benefitDefinition;

    /** Numeric value for simple perks (e.g., discount percent or delivery threshold). */
    @Column(precision = 12, scale = 2)
    private BigDecimal numericValue;

    /** Free-form JSON for richer perk configuration without a schema change. */
    /*@Column(length = 1024)
    private String configJson;*/

    @Column(nullable = false)
    private boolean active = true;

    protected TierBenefit() {
    }

    public TierBenefit(Tier tier, BenefitDefinition benefitDefinition, BigDecimal numericValue,
                       String configJson, boolean active) {
        this.tier = tier;
        this.benefitDefinition = benefitDefinition;
        this.numericValue = numericValue;
        //this.configJson = configJson;
        this.active = active;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public BenefitDefinition getBenefitDefinition() {
        return benefitDefinition;
    }

    public void setBenefitDefinition(BenefitDefinition benefitDefinition) {
        this.benefitDefinition = benefitDefinition;
    }

    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    /*public String getConfigJson() {
        return configJson;
    }*/

/*
    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }
*/

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
