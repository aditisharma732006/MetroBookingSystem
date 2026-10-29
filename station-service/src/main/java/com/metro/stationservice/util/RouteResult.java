package com.metro.stationservice.util;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

// NEW: carries the three Dijkstra maps back to the caller, so they are no longer fields of the singleton service
@Getter
@Setter
public class RouteResult {
    private Map<Long, Double> distances;
    private Map<Long, Long> previousStation;
    private Map<Long, Long> lineUsedToReach;
}