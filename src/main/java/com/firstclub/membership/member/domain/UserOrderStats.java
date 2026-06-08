package com.firstclub.membership.member.domain;

import com.firstclub.membership.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

/**
 * Rolling order statistics per user, updated from inbound order events and used as
 * input to tier evaluation. {@code @Version} guards concurrent updates from parallel
 * order events for the same user.
 */
@Entity
@Table(name = "user_order_stats")
public class UserOrderStats extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String userId;

    @Column(nullable = false)
    private long totalOrderCount;

    /** Order count within the rolling window used by tier rules. */
    @Column(nullable = false)
    private long rollingOrderCount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal currentMonthOrderValue = BigDecimal.ZERO;

    /** Calendar month (yyyy-MM) the monthly value applies to; resets on month change. */
    @Column(nullable = false, length = 7)
    private String monthKey;

    @Version
    private long version;

    protected UserOrderStats() {
    }

    public UserOrderStats(String userId, String monthKey) {
        this.userId = userId;
        this.monthKey = monthKey;
        this.totalOrderCount = 0;
        this.rollingOrderCount = 0;
        this.currentMonthOrderValue = BigDecimal.ZERO;
    }

    /** Applies a single order, rolling the monthly bucket when the month changes. */
    public void applyOrder(BigDecimal orderValue, String orderMonthKey) {
        this.totalOrderCount += 1;
        this.rollingOrderCount += 1;
        if (!orderMonthKey.equals(this.monthKey)) {
            this.monthKey = orderMonthKey;
            this.currentMonthOrderValue = BigDecimal.ZERO;
        }
        this.currentMonthOrderValue = this.currentMonthOrderValue.add(orderValue);
    }

    public String getUserId() {
        return userId;
    }

    public long getTotalOrderCount() {
        return totalOrderCount;
    }

    public long getRollingOrderCount() {
        return rollingOrderCount;
    }

    public BigDecimal getCurrentMonthOrderValue() {
        return currentMonthOrderValue;
    }

    public String getMonthKey() {
        return monthKey;
    }

    public long getVersion() {
        return version;
    }
}
