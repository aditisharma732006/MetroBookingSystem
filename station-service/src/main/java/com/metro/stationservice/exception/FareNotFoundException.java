package com.metro.stationservice.exception;

public class FareNotFoundException extends RuntimeException {
    public FareNotFoundException(double distanceKm) {
        super("No fare slab found for distance: " + distanceKm);
    }
}