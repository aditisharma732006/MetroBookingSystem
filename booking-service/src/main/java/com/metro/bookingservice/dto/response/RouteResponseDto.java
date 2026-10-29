package com.metro.bookingservice.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RouteResponseDto {
	 private List<PathStepDto> path;
    private double totalDistanceKm;
    private BigDecimal fare;
}