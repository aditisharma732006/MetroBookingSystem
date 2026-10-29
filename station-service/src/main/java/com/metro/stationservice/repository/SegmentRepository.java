package com.metro.stationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.metro.stationservice.entity.Segment;

public interface SegmentRepository extends JpaRepository<Segment, Long> {

}
