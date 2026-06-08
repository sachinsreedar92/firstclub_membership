package com.firstclub.membership.event.publisher;

import com.firstclub.membership.event.domain.OutboxEvent;
import com.firstclub.membership.event.repository.OutboxEventRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically drains the outbox. In this self-contained build, in-process listeners already
 * received each event after commit, so the relay confirms delivery by transitioning PENDING rows
 * to PUBLISHED. With a real broker this is exactly where rows would be sent and acknowledged,
 * giving at-least-once delivery that survives restarts.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 200;

    private final OutboxEventRepository outboxRepository;

    public OutboxRelay(OutboxEventRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelayString = "${membership.outbox.relay-interval-ms:5000}")
    @Transactional
    public void relayPending() {
        List<OutboxEvent> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxEvent.Status.PENDING, Limit.of(BATCH_SIZE));
        if (pending.isEmpty()) {
            return;
        }
        pending.forEach(OutboxEvent::markPublished);
        outboxRepository.saveAll(pending);
        log.debug("Outbox relay confirmed {} events", pending.size());
    }
}
