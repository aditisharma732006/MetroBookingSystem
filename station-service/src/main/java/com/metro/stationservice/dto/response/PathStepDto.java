package com.metro.stationservice.dto.response;

import lombok.Data;

//StationDto.java stays the same
//New: PathStepDto.java — one entry per station IN the journey

@Data
public class PathStepDto {
    private StationDto station;
    private LineDto line; 
}