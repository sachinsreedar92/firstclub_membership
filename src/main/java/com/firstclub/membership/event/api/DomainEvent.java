package com.firstclub.membership.event.api;

import java.time.Instant;

/**
 * Marker contract for all domain events. Keeping a common type lets the publisher, outbox,
 * and listeners treat events uniformly while remaining strongly typed at the call site.
 */
public interface DomainEvent {

    /** Stable event name used for the outbox and metrics (e.g., {@code SubscriptionCreated}). */
    String eventType();

    /** Aggregate this event is about (subscription id, user id, ...). */
    String aggregateId();

    /** Aggregate category, used by the outbox (e.g., {@code Subscription}, {@code Member}). */
    String aggregateType();

    Instant occurredAt();
}
