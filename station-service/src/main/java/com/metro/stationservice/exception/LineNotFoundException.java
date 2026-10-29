package com.metro.stationservice.exception;

public class LineNotFoundException extends RuntimeException{
	public LineNotFoundException(Long id) {
		super("Line not found with id: "+id);
	}
	
}
