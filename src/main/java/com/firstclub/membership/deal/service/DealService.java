package com.firstclub.membership.deal.service;

import com.firstclub.membership.deal.domain.Deal;
import com.firstclub.membership.deal.domain.DealPlanAccess;
import com.firstclub.membership.deal.dto.CachedDeal;
import com.firstclub.membership.deal.dto.DealRequest;
import com.firstclub.membership.deal.dto.DealResponse;
import com.firstclub.membership.deal.dto.DealTierAccessRequest;
import com.firstclub.membership.deal.dto.PublicDealView;
import java.util.List;

/**
 * Resolves which deals a member can currently access based on their membership plan and time.
 *
 * <p>Deals are plan-based (not tier-based): exclusive deals and early access are configured per
 * plan via {@link DealPlanAccess} rows, so premium plans receive earlier access timestamps.
 * The active deal catalog is cached as immutable snapshots for fast reads on the hot path.
 */
public interface DealService {

    List<CachedDeal> activeDeals();

    List<DealResponse> accessibleDeals(String userId);

    List<PublicDealView> listAllDeals();

    Deal createDeal(DealRequest request);

    DealPlanAccess grantPlanAccess(Long dealId, DealTierAccessRequest request);
}
