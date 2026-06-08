package com.firstclub.membership.observability;

import com.firstclub.membership.event.api.DomainEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Emits a Micrometer counter for every domain event, tagged by event type. Surfaces business
 * throughput (subscriptions, tier changes, orders) on the {@code /actuator/prometheus} endpoint
 * without coupling producers to the metrics system.
 */
@Component
public class EventMetricsListener {

    private final MeterRegistry meterRegistry;

    public EventMetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @EventListener
    public void onDomainEvent(DomainEvent event) {
        meterRegistry.counter("membership.events", "type", event.eventType()).increment();
    }
}
