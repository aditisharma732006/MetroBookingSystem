package com.metro.bookingservice.entity;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    CANCELLED,
    EXPIRED,
    PAID_AFTER_EXPIRY,   // money taken, booking dead, refund requested
    REFUNDED             // refund accepted by Razorpay; bank credit takes days
}