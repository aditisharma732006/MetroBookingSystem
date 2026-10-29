package com.metro.stationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//LineCreateDto.java — request
@Data
public class LineCreateDto {
	
    @NotBlank(message = "Line name cannot be blank")
	private String name;
	private String color;
}
