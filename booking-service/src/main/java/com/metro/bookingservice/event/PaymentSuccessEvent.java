package com.metro.bookingservice.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentSuccessEvent {
    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private String razorpayPaymentId;
}