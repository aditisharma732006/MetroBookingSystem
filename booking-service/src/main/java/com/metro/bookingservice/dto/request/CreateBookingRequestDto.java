package com.metro.bookingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookingRequestDto {
	@NotNull
	private Long fromStationId;
	
	@NotNull
	private Long toStationId;
	
}
