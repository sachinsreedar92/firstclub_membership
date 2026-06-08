package com.firstclub.membership.event.api;

/**
 * Outbound port for publishing domain events. The default adapter writes to a transactional
 * outbox and republishes over the in-process Spring event bus; swapping in a Kafka/Rabbit
 * adapter requires no change to producers.
 */
public interface EventPublisher {

    void publish(DomainEvent event);
}
