package com.metro.stationservice.exception;

public class SameStationException extends RuntimeException {
    public SameStationException() {
        super("Source and destination stations cannot be the same");
    }
}