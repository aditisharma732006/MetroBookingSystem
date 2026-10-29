package com.metro.stationservice.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

//RouteResponseDto.java — the merged path+distance+fare+interchange response
//USE CASE: user picks source + destination, taps "search". This single
//response has to answer EVERYTHING they need to decide whether to book
@Data
public class RouteResponseDto {
    private List<PathStepDto> path;
	private double totalDistanceKm;
	private BigDecimal fare;
}