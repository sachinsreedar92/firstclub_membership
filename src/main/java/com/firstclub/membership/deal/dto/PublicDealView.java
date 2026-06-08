package com.firstclub.membership.deal.dto;

import com.firstclub.membership.deal.domain.DealType;
import java.time.Instant;
import java.util.List;

/**
 * Full deal view suitable for the <b>product listing / promotions page</b>.
 *
 * <p>Includes which plans have access and each plan's access start so the UI can show:
 * <ul>
 *   <li>"Exclusive for Yearly / Quarterly subscribers"</li>
 *   <li>"Early access: Yearly members now, Quarterly members tomorrow, public in 2 days"</li>
 * </ul>
 */
public record PublicDealView(
        Long id,
        String code,
        String title,
        String description,
        DealType type,
        boolean exclusive,
        Instant publicStartAt,
        Instant endAt,
        boolean active,
        List<PlanAccessInfo> planAccess) {

    public record PlanAccessInfo(Long planId, String planCode, Instant accessStartAt, boolean earlyAccess) {}
}
