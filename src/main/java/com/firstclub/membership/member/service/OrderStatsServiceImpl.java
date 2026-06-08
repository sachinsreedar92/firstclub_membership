package com.firstclub.membership.member.service;

import com.firstclub.membership.common.concurrency.OptimisticRetry;
import com.firstclub.membership.member.domain.Member;
import com.firstclub.membership.member.domain.UserOrderStats;
import com.firstclub.membership.member.repository.UserOrderStatsRepository;
import com.firstclub.membership.tier.evaluation.TierEvaluationContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Maintains rolling per-user order statistics from inbound order events. Updates use optimistic
 * locking with retry so concurrent order events for the same user are applied correctly without
 * pessimistic locks (high-concurrency friendly).
 */
@Service
public class OrderStatsServiceImpl implements OrderStatsService {

    private static final Logger log = LoggerFactory.getLogger(OrderStatsServiceImpl.class);

    private final UserOrderStatsRepository statsRepository;
    private final MemberService memberService;
    private final OptimisticRetry optimisticRetry;
    private final TransactionTemplate requiresNewTx;

    public OrderStatsServiceImpl(UserOrderStatsRepository statsRepository,
                             MemberService memberService,
                             OptimisticRetry optimisticRetry,
                             PlatformTransactionManager transactionManager) {
        this.statsRepository = statsRepository;
        this.memberService = memberService;
        this.optimisticRetry = optimisticRetry;
        this.requiresNewTx = new TransactionTemplate(transactionManager);
        this.requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public TierEvaluationContext applyOrder(String userId, BigDecimal orderValue, Instant occurredAt) {
        String monthKey = YearMonth.from(occurredAt.atZone(ZoneOffset.UTC)).toString();
        log.debug("Applying order for user: {} - orderValue: {}, monthKey: {}", userId, orderValue, monthKey);
        return optimisticRetry.execute("applyOrder#" + userId, () ->
                requiresNewTx.execute(status -> doApplyOrder(userId, orderValue, monthKey)));
    }

    private TierEvaluationContext doApplyOrder(String userId, BigDecimal orderValue, String monthKey) {
        log.debug("Performing order stats mutation for user: {}", userId);
        UserOrderStats stats = statsRepository.findByUserId(userId).orElse(null);
        if (stats == null) {
            stats = new UserOrderStats(userId, monthKey);
            log.debug("Creating new order stats for user: {}", userId);
        }
        stats.applyOrder(orderValue, monthKey);
        try {
            stats = statsRepository.saveAndFlush(stats);
            log.debug("Order stats updated for user: {} - rollingCount: {}, currentMonthValue: {}",
                     userId, stats.getRollingOrderCount(), stats.getCurrentMonthOrderValue());
        } catch (DataIntegrityViolationException ex) {
            // Concurrent create for the same new user; reload and reapply on retry.
            log.warn("Concurrent order stats creation for user: {}, will retry", userId);
            throw new org.springframework.dao.OptimisticLockingFailureException(
                    "Concurrent stats creation for " + userId, ex);
        }
        Member member = memberService.findMember(userId);
        String cohort = member == null ? null : member.getCohort();
        return new TierEvaluationContext(
                userId, cohort, stats.getRollingOrderCount(), stats.getCurrentMonthOrderValue());
    }

    public TierEvaluationContext currentContext(String userId) {
        log.debug("Getting current tier evaluation context for user: {}", userId);
        UserOrderStats stats = statsRepository.findByUserId(userId).orElse(null);
        Member member = memberService.findMember(userId);
        String cohort = member == null ? null : member.getCohort();
        if (stats == null) {
            log.debug("No order stats found for user: {}", userId);
            return new TierEvaluationContext(userId, cohort, 0, BigDecimal.ZERO);
        }
        return new TierEvaluationContext(
                userId, cohort, stats.getRollingOrderCount(), stats.getCurrentMonthOrderValue());
    }
}
