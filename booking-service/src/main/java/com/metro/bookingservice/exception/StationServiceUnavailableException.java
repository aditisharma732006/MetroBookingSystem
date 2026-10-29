package com.metro.bookingservice.exception;

public class StationServiceUnavailableException extends RuntimeException {
    public StationServiceUnavailableException(String message) {
        super(message);
    }
}