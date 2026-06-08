package com.firstclub.membership.member.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A platform user participating in the rewards program. {@code currentTierId} is a
 * denormalized pointer to the member's active tier for fast reads; it is kept in sync
 * by the tier evaluation engine.
 */
@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String userId;

    @Column(length = 64)
    private String cohort;

    @Column
    private Long currentTierId;

    protected Member() {
    }

    public Member(String userId, String cohort, Long currentTierId) {
        this.userId = userId;
        this.cohort = cohort;
        this.currentTierId = currentTierId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCohort() {
        return cohort;
    }

    public void setCohort(String cohort) {
        this.cohort = cohort;
    }

    public Long getCurrentTierId() {
        return currentTierId;
    }

    public void setCurrentTierId(Long currentTierId) {
        this.currentTierId = currentTierId;
    }
}
