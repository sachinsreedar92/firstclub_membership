package com.firstclub.membership.tier.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A membership tier (e.g., Silver, Gold, Platinum). Tiers are data-driven: adding
 * a new tier requires no code change, only a new row plus its rules and benefits.
 * {@code level} defines ordering used to decide upgrades vs downgrades.
 */
@Entity
@Table(name = "tier")
public class Tier extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, unique = true)
    private int level;

    @Column(nullable = false)
    private boolean active = true;

    protected Tier() {
    }

    public Tier(String code, String name, int level, boolean active) {
        this.code = code;
        this.name = name;
        this.level = level;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
