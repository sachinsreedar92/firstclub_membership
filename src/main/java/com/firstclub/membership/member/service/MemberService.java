package com.firstclub.membership.member.service;

import com.firstclub.membership.member.domain.Member;

/**
 * Manages member profiles. {@code ensureMember} provides a get-or-create entry point used by the
 * subscribe flow and inbound order events so a member always exists before tier evaluation runs.
 */

public interface MemberService {

    Member ensureMember(String userId, String cohort);
    Member requireMember(String userId);
    Member findMember(String userId);
    void updateCurrentTier(String userId, Long tierId);
}
