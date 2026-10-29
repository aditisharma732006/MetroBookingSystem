package com.metro.stationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Segment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="station_a_id" , nullable=false)
	private Station stationA;
	
	@ManyToOne
	@JoinColumn(name="station_b_id" , nullable=false)
	private Station stationB;
	
	@ManyToOne
	@JoinColumn(name="line_id" , nullable=false)
	private Line line;
	
	@Column(nullable=false)
	private double distanceKm;
}
