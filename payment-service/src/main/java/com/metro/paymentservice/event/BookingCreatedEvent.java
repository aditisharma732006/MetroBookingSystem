package com.metro.paymentservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
public class BookingCreatedEvent {
    private String eventId;        // UUID, generated once; used for idempotency in Phase 6
    private String eventType;      // "BOOKING_CREATED"
    private Instant occurredAt;    // lets Payment skip stale events
    private Long bookingId;
    private Long userId;
    private BigDecimal fare;
    private String ticketCode;
}
