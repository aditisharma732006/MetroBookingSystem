package com.metro.paymentservice.event;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSuccessEvent {
    private String eventId;
    private String eventType;          // "PAYMENT_SUCCESS"
    private Instant occurredAt;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private String razorpayPaymentId;
}