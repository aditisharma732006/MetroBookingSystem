package com.metro.stationservice.dto.response;

import java.math.BigDecimal;

import lombok.Data;

//SegmentDto.java — response, names resolved (for map display) + ids (for clickable UI)
@Data
public class SegmentDto {
 private Long id;
 private Long stationAId;
 private String stationAName;
 private Long stationBId;
 private String stationBName;
 private Long lineId;
 private String lineName;
 private double distanceKm;
}