package com.firstclub.membership.event.service;

import com.firstclub.membership.event.dto.OrderEventRequest;

/**
 * Entry point for inbound order signals. Ensures the member exists and publishes an
 * {@code OrderPlaced} domain event inside a transaction, so the transactional listener that drives
 * tier evaluation fires only after the order is durably recorded in the outbox.
 */

public interface OrderIngestionService {

    String ingest(OrderEventRequest request);
}
