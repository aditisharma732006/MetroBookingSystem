package com.metro.bookingservice.event;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequiredEvent {
    private String eventId;
    private String eventType;     // "REFUND_REQUIRED"
    private Instant occurredAt;
    private Long bookingId;
    private String reason;
}