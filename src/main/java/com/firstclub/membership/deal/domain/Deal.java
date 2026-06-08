package com.firstclub.membership.deal.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A deal or sale campaign. Tier-based access is expressed via {@link DealTierAccess} rows:
 * <ul>
 *   <li>EXCLUSIVE_DEAL + {@code exclusive=true}: only tiers with an access row can see it.</li>
 *   <li>SALE: available to everyone from {@code publicStartAt}; higher tiers may be granted an
 *       earlier {@code accessStartAt} for early access.</li>
 * </ul>
 */
@Entity
@Table(name = "deal")
public class Deal extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private DealType type;

    /** When the deal becomes publicly available; null means it is access-gated only. */
    @Column
    private Instant publicStartAt;

    @Column(nullable = false)
    private Instant endAt;

    @Column(nullable = false)
    private boolean exclusive;

    @Column(nullable = false)
    private boolean active = true;

    protected Deal() {
    }

    public Deal(String code, String title, String description, DealType type,
                Instant publicStartAt, Instant endAt, boolean exclusive, boolean active) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.type = type;
        this.publicStartAt = publicStartAt;
        this.endAt = endAt;
        this.exclusive = exclusive;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DealType getType() {
        return type;
    }

    public Instant getPublicStartAt() {
        return publicStartAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public boolean isActive() {
        return active;
    }
}
