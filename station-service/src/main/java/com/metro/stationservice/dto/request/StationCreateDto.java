package com.metro.stationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//StationCreateDto.java — request (no id, client doesn't set it)
//for admin to add station
@Data
public class StationCreateDto {

	@NotBlank(message = "Station name cannot be blnk")
	private String name;
	
	@NotBlank(message = "Station code cannot be blank")
	private String code;
}
