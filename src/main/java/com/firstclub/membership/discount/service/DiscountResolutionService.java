package com.firstclub.membership.discount.service;

import com.firstclub.membership.discount.dto.DiscountContext;
import com.firstclub.membership.discount.dto.DiscountResult;
import com.firstclub.membership.discount.dto.PlanDiscountView;

/**
 * Discount resolution service — two usage modes:
 *
 * <ul>
 *   <li><b>Product listing page</b>: {@link #listPlanDiscounts(String)} returns <em>all</em>
 *       active discount badges for the user's plan so the UI can annotate every product with the
 *       correct badge without making per-product API calls.</li>
 *   <li><b>Cart / checkout validation</b>: {@link #resolve(String, DiscountContext)} applies
 *       "most-specific-wins" to a single product and returns the exact discount percentage.</li>
 * </ul>
 *
 * Eligibility rows are served from cache, so both paths are suitable for high-traffic reads.
 */

public interface DiscountResolutionService {

    PlanDiscountView listPlanDiscounts(String userId);
    DiscountResult resolve(String userId, DiscountContext context);
}
