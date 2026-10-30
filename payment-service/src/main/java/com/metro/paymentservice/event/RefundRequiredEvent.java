package com.metro.paymentservice.event;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundRequiredEvent {
    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private Long bookingId;
    private String reason;
}