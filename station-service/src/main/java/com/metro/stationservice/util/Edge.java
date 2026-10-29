package com.metro.stationservice.util;



public class Edge {

	private Long neighborStationId;
    private double distanceKm;
    private Long lineId;
    
    public Edge() {}
    
    public Edge(Long neighborStationId , double distanceKm, Long lineId) {
    	this.neighborStationId=neighborStationId;
    	this.distanceKm=distanceKm;
    	this.lineId=lineId;
		
	}
    
	public Long getNeighborStationId() {
		return neighborStationId;
	}
	public void setNeighborStationId(Long neighborStationId) {
		this.neighborStationId = neighborStationId;
	}
	public double getDistanceKm() {
		return distanceKm;
	}
	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}

	public Long getLineId() {
		return lineId;
	}

	public void setLineId(Long lineId) {
		this.lineId = lineId;
	}
	
    
    
}
