package com.metro.stationservice.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.metro.stationservice.dto.request.SegmentCreateDto;
import com.metro.stationservice.dto.response.SegmentDto;
import com.metro.stationservice.entity.Segment;
import com.metro.stationservice.exception.LineNotFoundException;
import com.metro.stationservice.exception.StationNotFoundException;
import com.metro.stationservice.repository.LineRepository;
import com.metro.stationservice.repository.SegmentRepository;
import com.metro.stationservice.repository.StationRepository;

@RestController
@RequestMapping("/segments")
public class SegmentController {

	@Autowired
	public SegmentRepository segmentRepository;
	
	@Autowired
	public StationRepository stationRepository;
	
	@Autowired
	public LineRepository lineRepository;
	
	@GetMapping
	public List<SegmentDto> getAllSegments(){
		return segmentRepository.findAll().stream().map(
				seg ->{
					SegmentDto dto = new SegmentDto();
					dto.setId(seg.getId());
					dto.setStationAId(seg.getStationA().getId());
					dto.setStationBId(seg.getStationB().getId());
					dto.setStationAName(seg.getStationA().getName());
					dto.setStationBName(seg.getStationB().getName());
					dto.setLineId(seg.getLine().getId());
					dto.setLineName(seg.getLine().getName());
					dto.setDistanceKm(seg.getDistanceKm());
					return dto;
					
				}).collect(Collectors.toList());
		}
	
	@PostMapping
	public SegmentDto addSegment(@RequestBody SegmentCreateDto request) {
		Segment segment=new Segment();
		segment.setStationA(stationRepository.findById(request.getStationAId()).orElseThrow(
				()-> new StationNotFoundException(request.getStationAId())));
		segment.setStationB(stationRepository.findById(request.getStationBId()).orElseThrow(
				()-> new StationNotFoundException(request.getStationBId())));
		segment.setLine(lineRepository.findById(request.getLineId()).orElseThrow(
				() -> new LineNotFoundException(request.getLineId())));
		segment.setDistanceKm(request.getDistanceKm());
		
		Segment saved = segmentRepository.save(segment);
		
		SegmentDto dto = new SegmentDto();
		
		dto.setId(saved.getId());
		dto.setStationAId(saved.getStationA().getId());
		dto.setStationBId(saved.getStationB().getId());
		dto.setStationAName(saved.getStationA().getName());
		dto.setStationBName(saved.getStationB().getName());
		dto.setLineId(saved.getLine().getId());
		dto.setLineName(saved.getLine().getName());
		dto.setDistanceKm(saved.getDistanceKm());
		
		return dto;
		
	}
	
	@DeleteMapping("/{id}")
	public void removeSegment(@PathVariable Long id) {
		segmentRepository.deleteById(id);
	}

}
