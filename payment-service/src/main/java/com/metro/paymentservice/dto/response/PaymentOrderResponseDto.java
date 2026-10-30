package com.metro.paymentservice.dto.response;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentOrderResponseDto {
    private Long bookingId;
    private String razorpayOrderId;
    private BigDecimal amount;   // rupees, for display only
    private String currency;
    private String keyId;        // public Razorpay key id, Checkout needs it
}