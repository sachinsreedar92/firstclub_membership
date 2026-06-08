package com.firstclub.membership.member.service;

import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.member.domain.Member;
import com.firstclub.membership.member.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages member profiles. {@code ensureMember} provides a get-or-create entry point used by the
 * subscribe flow and inbound order events so a member always exists before tier evaluation runs.
 */
@Service
public class MemberServiceImpl implements MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member ensureMember(String userId, String cohort) {
        log.debug("Ensuring member exists for user: {} with cohort: {}", userId, cohort);
        Member member = memberRepository.findByUserId(userId).orElse(null);
        if (member == null) {
            Member newMember = new Member(userId, cohort, null);
            member = memberRepository.save(newMember);
            log.info("New member created - userId={}, cohort={}", userId, cohort);
            return member;
        }
        // Backfill cohort if it was unknown at creation time but is now supplied.
        if (cohort != null && !cohort.isBlank() && member.getCohort() == null) {
            log.debug("Backfilling cohort for member: {} with cohort: {}", userId, cohort);
            member.setCohort(cohort);
            member = memberRepository.save(member);
        }
        return member;
    }

    @Transactional(readOnly = true)
    public Member requireMember(String userId) {
        log.debug("Retrieving required member: {}", userId);
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Member not found: {}", userId);
                    return ResourceNotFoundException.of("Member", userId);
                });
    }

    @Transactional(readOnly = true)
    public Member findMember(String userId) {
        log.debug("Looking up member: {}", userId);
        return memberRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public void updateCurrentTier(String userId, Long tierId) {
        log.debug("Updating current tier for user: {} to tier id: {}", userId, tierId);
        Member member = requireMember(userId);
        member.setCurrentTierId(tierId);
        memberRepository.save(member);
        log.debug("Current tier updated for user: {} to tier id: {}", userId, tierId);
    }
}
