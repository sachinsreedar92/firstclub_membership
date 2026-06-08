package com.firstclub.membership.event.publisher;

import com.firstclub.membership.event.api.DomainEvent;
import com.firstclub.membership.event.api.EventPublisher;
import com.firstclub.membership.event.domain.OutboxEvent;
import com.firstclub.membership.event.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Default {@link EventPublisher} adapter implementing the transactional outbox pattern:
 * <ol>
 *   <li>the event is persisted as a PENDING {@link OutboxEvent} in the caller's transaction, and</li>
 *   <li>it is handed to the in-process Spring bus, where {@code @TransactionalEventListener}s
 *       receive it only after that transaction commits.</li>
 * </ol>
 * This is the single swap point for a distributed broker: a Kafka/Rabbit adapter would publish the
 * same outbox rows without any change to event producers.
 */
@Component
public class OutboxEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository outboxRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxEventRepository outboxRepository,
                                ApplicationEventPublisher applicationEventPublisher,
                                ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        outboxRepository.save(new OutboxEvent(
                event.aggregateType(), event.aggregateId(), event.eventType(), serialize(event)));
        applicationEventPublisher.publishEvent(event);
        log.debug("Published event {} for {}#{}",
                event.eventType(), event.aggregateType(), event.aggregateId());
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event {}, storing type only", event.eventType(), e);
            return "{\"eventType\":\"" + event.eventType() + "\"}";
        }
    }
}
