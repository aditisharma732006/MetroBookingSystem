package com.metro.paymentservice.exception;

public class PaymentNotReadyException extends RuntimeException {
    public PaymentNotReadyException(String message) { super(message); }
}