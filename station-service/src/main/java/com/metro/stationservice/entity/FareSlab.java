package com.metro.stationservice.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class FareSlab {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private double minDistanceKm;
	
	@Column(nullable=false)
	private double maxDistanceKm;
	
	@Column(nullable=false)
	private BigDecimal fare;
	
}
