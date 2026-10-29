package com.metro.bookingservice.exception;

public class EventPublishException extends RuntimeException {
    public EventPublishException(String message) {
        super(message);
    }
}