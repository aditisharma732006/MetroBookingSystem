package com.metro.bookingservice.event;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

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