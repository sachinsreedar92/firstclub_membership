package com.firstclub.membership.benefit.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Catalog definition of a benefit. The concrete value granted to a member is held
 * per-tier in {@link com.firstclub.membership.benefit.domain.TierBenefit}.
 */
@Entity
@Table(name = "benefit_definition")
public class BenefitDefinition extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BenefitType type;

    @Column(nullable = false, length = 256)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    protected BenefitDefinition() {
    }

    public BenefitDefinition(String code, BenefitType type, String description, boolean active) {
        this.code = code;
        this.type = type;
        this.description = description;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BenefitType getType() {
        return type;
    }

    public void setType(BenefitType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
