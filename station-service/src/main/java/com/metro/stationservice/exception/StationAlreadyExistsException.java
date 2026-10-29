package com.metro.stationservice.exception;

public class StationAlreadyExistsException extends RuntimeException {
    public StationAlreadyExistsException(String name) {
        super("Station already exists: " + name);
    }
}