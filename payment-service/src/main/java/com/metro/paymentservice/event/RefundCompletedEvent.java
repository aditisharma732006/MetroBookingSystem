package com.metro.paymentservice.event;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundCompletedEvent {
    private String eventId;
    private String eventType;     // "REFUND_COMPLETED"
    private Instant occurredAt;
    private Long bookingId;
}