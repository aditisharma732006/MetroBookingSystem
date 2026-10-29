package com.metro.stationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.metro.stationservice.entity.Line;

public interface LineRepository extends JpaRepository<Line, Long>{

}
