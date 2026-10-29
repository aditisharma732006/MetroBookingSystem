package com.metro.stationservice.exception;

// NEW: thrown when 'to' is not in the graph or not connected to 'from'
public class RouteNotReachableException extends RuntimeException {
    public RouteNotReachableException(Long from, Long to) {
        super("No route from station " + from + " to station " + to);
    }
}