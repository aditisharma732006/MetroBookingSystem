package com.metro.stationservice.dto.response;

import lombok.Data;

@Data
public class StationDto {
    private Long id;
    private String name;
    private String code;
}

//for list of stations and id to let user select the source or destination station