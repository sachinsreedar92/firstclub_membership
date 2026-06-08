package com.firstclub.membership.member.service;

import com.firstclub.membership.benefit.dto.EffectiveBenefit;
import com.firstclub.membership.member.dto.MembershipView;
import java.util.List;

/**
 * Facade that composes a member's profile, active subscription, current plan, current tier and
 * resolved benefits into a single view.  Keeps controllers thin and hides cross-feature wiring
 * behind one entry point (Facade pattern).
 */

public interface MembershipFacade {

    MembershipView getMembership(String userId);
    List<EffectiveBenefit> getMemberBenefits(String userId);
}
