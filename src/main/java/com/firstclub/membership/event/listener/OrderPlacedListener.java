package com.firstclub.membership.event.listener;

import com.firstclub.membership.event.api.EventPublisher;
import com.firstclub.membership.event.api.MembershipEvents;
import com.firstclub.membership.member.domain.Member;
import com.firstclub.membership.member.service.MemberService;
import com.firstclub.membership.member.service.OrderStatsService;
import com.firstclub.membership.subscription.service.SubscriptionService;
import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.evaluation.TierEvaluationContext;
import com.firstclub.membership.tier.evaluation.TierEvaluationService;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to {@code OrderPlaced} events to drive tier progression. Runs after the producing
 * transaction commits and asynchronously on a virtual thread, so order ingestion stays fast while
 * stats accumulation and tier evaluation happen out of band.
 */
@Component
public class OrderPlacedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedListener.class);

    private final OrderStatsService orderStatsService;
    private final TierEvaluationService tierEvaluationService;
    private final SubscriptionService subscriptionService;
    private final MemberService memberService;
    private final EventPublisher eventPublisher;

    public OrderPlacedListener(OrderStatsService orderStatsService,
                               TierEvaluationService tierEvaluationService,
                               SubscriptionService subscriptionService,
                               MemberService memberService,
                               EventPublisher eventPublisher) {
        this.orderStatsService = orderStatsService;
        this.tierEvaluationService = tierEvaluationService;
        this.subscriptionService = subscriptionService;
        this.memberService = memberService;
        this.eventPublisher = eventPublisher;
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(fallbackExecution = true)
    public void onOrderPlaced(MembershipEvents.OrderPlacedEvent event) {
        TierEvaluationContext context = orderStatsService.applyOrder(
                event.userId(), event.orderValue(), event.occurredAt());

        Optional<Tier> bestTier = tierEvaluationService.evaluateBestTier(context);
        if (bestTier.isEmpty()) {
            return;
        }

        Member member = memberService.findMember(event.userId());
        Long currentTierId = member == null ? null : member.getCurrentTierId();
        Long targetTierId = bestTier.get().getId();

        if (targetTierId.equals(currentTierId)) {
            return;
        }

        subscriptionService.applyAutoTierChange(event.userId(), targetTierId);
        eventPublisher.publish(new MembershipEvents.MemberTierChangedEvent(
                event.userId(), currentTierId, targetTierId,
                "Auto-evaluated from order activity", Instant.now()));

        log.info("Member {} auto tier change {} -> {} after order {}",
                event.userId(), currentTierId, targetTierId, event.orderId());
    }
}
