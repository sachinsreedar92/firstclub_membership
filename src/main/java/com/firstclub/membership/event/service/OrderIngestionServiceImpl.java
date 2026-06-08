package com.firstclub.membership.event.service;

import com.firstclub.membership.event.api.EventPublisher;
import com.firstclub.membership.event.api.MembershipEvents;
import com.firstclub.membership.event.dto.OrderEventRequest;
import com.firstclub.membership.member.service.MemberService;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Entry point for inbound order signals. Ensures the member exists and publishes an
 * {@code OrderPlaced} domain event inside a transaction, so the transactional listener that drives
 * tier evaluation fires only after the order is durably recorded in the outbox.
 */
@Service
public class OrderIngestionServiceImpl implements OrderIngestionService {

    private static final Logger log = LoggerFactory.getLogger(OrderIngestionServiceImpl.class);

    private final MemberService memberService;
    private final EventPublisher eventPublisher;

    public OrderIngestionServiceImpl(MemberService memberService, EventPublisher eventPublisher) {
        this.memberService = memberService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String ingest(OrderEventRequest request) {
        log.info("Ingesting order event - userId: {}, cohort: {}, orderValue: {}",
                 request.userId(), request.cohort(), request.orderValue());
        memberService.ensureMember(request.userId(), request.cohort());
        String orderId = (request.orderId() == null || request.orderId().isBlank())
                ? UUID.randomUUID().toString()
                : request.orderId();
        log.debug("Publishing OrderPlaced event - orderId: {}, userId: {}, orderValue: {}",
                 orderId, request.userId(), request.orderValue());
        eventPublisher.publish(new MembershipEvents.OrderPlacedEvent(
                request.userId(), orderId, request.orderValue(), Instant.now()));
        log.info("Order ingested successfully - orderId: {}, userId: {}", orderId, request.userId());
        return orderId;
    }
}
