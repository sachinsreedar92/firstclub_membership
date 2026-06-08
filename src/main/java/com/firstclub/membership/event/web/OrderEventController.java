package com.firstclub.membership.event.web;

import com.firstclub.membership.event.dto.OrderEventRequest;
import com.firstclub.membership.event.service.OrderIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Publish order events to exercise the tier engine (demo/test)")
public class OrderEventController {

    private static final Logger log = LoggerFactory.getLogger(OrderEventController.class);

    private final OrderIngestionService orderIngestionService;

    public OrderEventController(OrderIngestionService orderIngestionService) {
        this.orderIngestionService = orderIngestionService;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Publish an OrderPlaced event (drives rolling stats + auto tier evaluation)")
    public Map<String, String> publishOrder(@Valid @RequestBody OrderEventRequest request) {
        log.info("POST /api/v1/events/orders - Publishing order event - userId: {}, orderValue: {}",
                 request.userId(), request.orderValue());
        String orderId = orderIngestionService.ingest(request);
        log.info("Order event published successfully - orderId: {}, userId: {}", orderId, request.userId());
        return Map.of("status", "accepted", "orderId", orderId);
    }
}
