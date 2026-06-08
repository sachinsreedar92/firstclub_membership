package com.firstclub.membership.subscription.service;

import com.firstclub.membership.common.concurrency.OptimisticRetry;
import com.firstclub.membership.common.exception.BadRequestException;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.event.api.EventPublisher;
import com.firstclub.membership.event.api.MembershipEvents;
import com.firstclub.membership.member.service.MemberService;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.service.MembershipPlanService;
import com.firstclub.membership.subscription.domain.IdempotencyRecord;
import com.firstclub.membership.subscription.domain.Subscription;
import com.firstclub.membership.subscription.domain.SubscriptionAction;
import com.firstclub.membership.subscription.domain.SubscriptionHistory;
import com.firstclub.membership.subscription.domain.SubscriptionStateMachine;
import com.firstclub.membership.subscription.domain.SubscriptionStatus;
import com.firstclub.membership.subscription.dto.SubscribeRequest;
import com.firstclub.membership.subscription.dto.SubscriptionResponse;
import com.firstclub.membership.subscription.repository.IdempotencyRecordRepository;
import com.firstclub.membership.subscription.repository.SubscriptionHistoryRepository;
import com.firstclub.membership.subscription.repository.SubscriptionRepository;
import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.service.TierService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Core subscription lifecycle service: subscribe, upgrade, downgrade, cancel, and auto tier
 * changes driven by the tier engine. Concurrency is handled with optimistic locking plus retry,
 * an idempotency key on subscribe, and the {@link SubscriptionStateMachine} guard for transitions.
 *
 * <p>Retried mutations run via a REQUIRES_NEW {@link TransactionTemplate} so each retry attempt
 * gets a fresh transaction (a failed optimistic lock marks the prior transaction rollback-only).
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final MembershipPlanService planService;
    private final TierService tierService;
    private final MemberService memberService;
    private final SubscriptionStateMachine stateMachine;
    private final OptimisticRetry optimisticRetry;
    private final EventPublisher eventPublisher;
    private final TransactionTemplate requiresNewTx;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                               SubscriptionHistoryRepository historyRepository,
                               IdempotencyRecordRepository idempotencyRepository,
                               MembershipPlanService planService,
                               TierService tierService,
                               MemberService memberService,
                               SubscriptionStateMachine stateMachine,
                               OptimisticRetry optimisticRetry,
                               EventPublisher eventPublisher,
                               PlatformTransactionManager transactionManager) {
        this.subscriptionRepository = subscriptionRepository;
        this.historyRepository = historyRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.planService = planService;
        this.tierService = tierService;
        this.memberService = memberService;
        this.stateMachine = stateMachine;
        this.optimisticRetry = optimisticRetry;
        this.eventPublisher = eventPublisher;
        this.requiresNewTx = new TransactionTemplate(transactionManager);
        this.requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional
    public SubscriptionResponse subscribe(SubscribeRequest request, String idempotencyKey) {
        // Idempotency: a replayed request returns the original subscription rather than duplicating.
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyRecord> existing = idempotencyRepository.findById(idempotencyKey);
            if (existing.isPresent()) {
                Subscription sub = subscriptionRepository.findById(existing.get().getSubscriptionId())
                        .orElseThrow(() -> ResourceNotFoundException.of(
                                "Subscription", existing.get().getSubscriptionId()));
                return SubscriptionResponse.from(sub);
            }
        }

        MembershipPlan plan = planService.requireActivePlan(request.planId());
        Tier tier = tierService.requireTier(request.tierId());
        memberService.ensureMember(request.userId(), null);

        subscriptionRepository.findByUserIdAndStatus(request.userId(), SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new ConflictException(
                            "User already has an active subscription: " + request.userId());
                });

        Instant now = Instant.now();
        Subscription subscription = Subscription.builder()
                .userId(request.userId())
                .planId(plan.getId())
                .tierId(tier.getId())
                .startDate(now)
                .expiryDate(now.plus(plan.getDurationDays(), ChronoUnit.DAYS))
                .autoRenew(request.autoRenew() == null || request.autoRenew())
                .build();

        Subscription saved;
        try {
            saved = subscriptionRepository.saveAndFlush(subscription);
        } catch (DataIntegrityViolationException ex) {
            // Lost the race against a concurrent subscribe for the same user (unique active key).
            throw new ConflictException(
                    "User already has an active subscription: " + request.userId());
        }

        recordHistory(SubscriptionHistory.builder()
                .subscriptionId(saved.getId())
                .userId(saved.getUserId())
                .action(SubscriptionAction.SUBSCRIBE)
                .toTierId(saved.getTierId())
                .toStatus(SubscriptionStatus.ACTIVE)
                .note("Subscribed to plan " + plan.getCode()));

        memberService.updateCurrentTier(saved.getUserId(), saved.getTierId());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyRepository.save(
                    new IdempotencyRecord(idempotencyKey, saved.getUserId(), saved.getId()));
        }

        eventPublisher.publish(new MembershipEvents.SubscriptionCreatedEvent(
                String.valueOf(saved.getId()), saved.getUserId(),
                saved.getPlanId(), saved.getTierId(), Instant.now()));

        log.info("Subscription created id={} user={} plan={} tier={}",
                saved.getId(), saved.getUserId(), plan.getCode(), tier.getCode());
        return SubscriptionResponse.from(saved);
    }

    public SubscriptionResponse upgrade(Long subscriptionId, Long targetTierId) {
        return changeTier(subscriptionId, targetTierId, SubscriptionAction.UPGRADE);
    }

    public SubscriptionResponse downgrade(Long subscriptionId, Long targetTierId) {
        return changeTier(subscriptionId, targetTierId, SubscriptionAction.DOWNGRADE);
    }

    private SubscriptionResponse changeTier(Long subscriptionId, Long targetTierId,
                                            SubscriptionAction action) {
        Tier targetTier = tierService.requireTier(targetTierId);
        return optimisticRetry.execute("changeTier#" + subscriptionId, () ->
                requiresNewTx.execute(status -> mutateTier(subscriptionId, targetTier, action)));
    }

    private SubscriptionResponse mutateTier(Long subscriptionId, Tier targetTier,
                                            SubscriptionAction action) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Subscription", subscriptionId));
        stateMachine.assertCanApply(subscription.getStatus(), action);

        Long fromTierId = subscription.getTierId();
        if (fromTierId.equals(targetTier.getId())) {
            throw new BadRequestException("Subscription is already on tier " + targetTier.getCode());
        }

        Tier currentTier = tierService.requireTier(fromTierId);
        boolean isUpgrade = targetTier.getLevel() > currentTier.getLevel();
        if (action == SubscriptionAction.UPGRADE && !isUpgrade) {
            throw new BadRequestException(
                    "Target tier is not higher than current; use downgrade instead");
        }
        if (action == SubscriptionAction.DOWNGRADE && isUpgrade) {
            throw new BadRequestException(
                    "Target tier is not lower than current; use upgrade instead");
        }

        subscription.changeTier(targetTier.getId());
        Subscription saved = subscriptionRepository.save(subscription);

        recordHistory(SubscriptionHistory.builder()
                .subscriptionId(saved.getId())
                .userId(saved.getUserId())
                .action(action)
                .fromTierId(fromTierId)
                .toTierId(targetTier.getId())
                .fromStatus(SubscriptionStatus.ACTIVE)
                .toStatus(SubscriptionStatus.ACTIVE)
                .note(action + " to " + targetTier.getCode()));

        memberService.updateCurrentTier(saved.getUserId(), targetTier.getId());

        eventPublisher.publish(new MembershipEvents.SubscriptionTierChangedEvent(
                String.valueOf(saved.getId()), saved.getUserId(), fromTierId,
                targetTier.getId(), action.name(), Instant.now()));

        log.info("Subscription {} tier changed {} -> {} ({})",
                saved.getId(), fromTierId, targetTier.getId(), action);
        return SubscriptionResponse.from(saved);
    }

    /**
     * Applies a tier change requested by the automatic tier-evaluation engine. No-op when the user
     * has no active subscription or is already on the target tier. Retries on optimistic conflict.
     */
    public void applyAutoTierChange(String userId, Long targetTierId) {
        Tier targetTier = tierService.requireTier(targetTierId);
        optimisticRetry.execute("autoTier#" + userId, () ->
                requiresNewTx.execute(status -> {
                    mutateAutoTier(userId, targetTier);
                    return null;
                }));
    }

    private void mutateAutoTier(String userId, Tier targetTier) {
        Optional<Subscription> active =
                subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        if (active.isEmpty()) {
            memberService.updateCurrentTier(userId, targetTier.getId());
            return;
        }
        Subscription subscription = active.get();
        Long fromTierId = subscription.getTierId();
        if (fromTierId.equals(targetTier.getId())) {
            return;
        }
        subscription.changeTier(targetTier.getId());
        subscriptionRepository.save(subscription);

        recordHistory(SubscriptionHistory.builder()
                .subscriptionId(subscription.getId())
                .userId(userId)
                .action(SubscriptionAction.TIER_AUTO_CHANGE)
                .fromTierId(fromTierId)
                .toTierId(targetTier.getId())
                .fromStatus(SubscriptionStatus.ACTIVE)
                .toStatus(SubscriptionStatus.ACTIVE)
                .note("Auto tier change to " + targetTier.getCode()));

        memberService.updateCurrentTier(userId, targetTier.getId());
        log.info("Auto tier change for user {} {} -> {}", userId, fromTierId, targetTier.getId());
    }

    @Transactional
    public SubscriptionResponse cancel(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Subscription", subscriptionId));
        stateMachine.assertCanApply(subscription.getStatus(), SubscriptionAction.CANCEL);

        subscription.deactivate(SubscriptionStatus.CANCELLED);
        Subscription saved = subscriptionRepository.save(subscription);

        recordHistory(SubscriptionHistory.builder()
                .subscriptionId(saved.getId())
                .userId(saved.getUserId())
                .action(SubscriptionAction.CANCEL)
                .fromStatus(SubscriptionStatus.ACTIVE)
                .toStatus(SubscriptionStatus.CANCELLED)
                .note("Subscription cancelled"));

        eventPublisher.publish(new MembershipEvents.SubscriptionCancelledEvent(
                String.valueOf(saved.getId()), saved.getUserId(), Instant.now()));

        log.info("Subscription {} cancelled for user {}", saved.getId(), saved.getUserId());
        return SubscriptionResponse.from(saved);
    }

    /** Marks active subscriptions whose expiry has passed as EXPIRED. Invoked by a scheduler. */
    @Transactional
    public int expireDueSubscriptions() {
        List<Subscription> due = subscriptionRepository
                .findByStatusAndExpiryDateBefore(SubscriptionStatus.ACTIVE, Instant.now());
        for (Subscription subscription : due) {
            subscription.deactivate(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            recordHistory(SubscriptionHistory.builder()
                    .subscriptionId(subscription.getId())
                    .userId(subscription.getUserId())
                    .action(SubscriptionAction.EXPIRE)
                    .fromStatus(SubscriptionStatus.ACTIVE)
                    .toStatus(SubscriptionStatus.EXPIRED)
                    .note("Subscription expired"));
        }
        if (!due.isEmpty()) {
            log.info("Expired {} subscriptions", due.size());
        }
        return due.size();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(String userId) {
        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active subscription for user: " + userId));
        return SubscriptionResponse.from(subscription);
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> findActiveSubscription(String userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .map(SubscriptionResponse::from);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionHistory(String userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    /**
     * Returns every tier-change event for a user in reverse-chronological order.
     * Includes manual upgrade/downgrade, auto tier changes, and the initial SUBSCRIBE event.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionHistory> getTierProgressionHistory(String userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(h -> h.getFromTierId() != null || h.getToTierId() != null)
                .toList();
    }

    private void recordHistory(SubscriptionHistory.Builder builder) {
        historyRepository.save(builder.build());
    }
}
