package com.metro.stationservice.exception;

public class StationNotFoundException extends RuntimeException{
	public StationNotFoundException(Long id) {
		super("Station not found with id: "+id);
	}

}
