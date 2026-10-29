package com.metro.stationservice.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FareSlabDto {
    private Long id;
    private double minDistanceKm;
    private double maxDistanceKm;
    private BigDecimal fare;
}