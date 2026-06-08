package com.firstclub.membership.event.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Concrete domain events. Grouped as small immutable records for discoverability; each is an
 * independent contract implementing {@link DomainEvent}.
 */
public final class MembershipEvents {

    private MembershipEvents() {
    }

    public record SubscriptionCreatedEvent(
            String subscriptionId, String userId, Long planId, Long tierId, Instant occurredAt)
            implements DomainEvent {
        @Override
        public String eventType() {
            return "SubscriptionCreated";
        }

        @Override
        public String aggregateId() {
            return subscriptionId;
        }

        @Override
        public String aggregateType() {
            return "Subscription";
        }
    }

    public record SubscriptionTierChangedEvent(
            String subscriptionId, String userId, Long fromTierId, Long toTierId,
            String reason, Instant occurredAt) implements DomainEvent {
        @Override
        public String eventType() {
            return "SubscriptionTierChanged";
        }

        @Override
        public String aggregateId() {
            return subscriptionId;
        }

        @Override
        public String aggregateType() {
            return "Subscription";
        }
    }

    public record SubscriptionCancelledEvent(
            String subscriptionId, String userId, Instant occurredAt) implements DomainEvent {
        @Override
        public String eventType() {
            return "SubscriptionCancelled";
        }

        @Override
        public String aggregateId() {
            return subscriptionId;
        }

        @Override
        public String aggregateType() {
            return "Subscription";
        }
    }

    /** Inbound signal from the shopping/checkout journey that feeds tier evaluation. */
    public record OrderPlacedEvent(
            String userId, String orderId, BigDecimal orderValue, Instant occurredAt)
            implements DomainEvent {
        @Override
        public String eventType() {
            return "OrderPlaced";
        }

        @Override
        public String aggregateId() {
            return userId;
        }

        @Override
        public String aggregateType() {
            return "Member";
        }
    }

    /** Emitted by the tier engine when a member crosses a tier boundary. */
    public record MemberTierChangedEvent(
            String userId, Long fromTierId, Long toTierId, String reason, Instant occurredAt)
            implements DomainEvent {
        @Override
        public String eventType() {
            return "MemberTierChanged";
        }

        @Override
        public String aggregateId() {
            return userId;
        }

        @Override
        public String aggregateType() {
            return "Member";
        }
    }
}
