package com.metro.stationservice.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.metro.stationservice.dto.request.StationCreateDto;
import com.metro.stationservice.dto.response.StationDto;
import com.metro.stationservice.entity.Station;
import com.metro.stationservice.exception.StationAlreadyExistsException;
import com.metro.stationservice.repository.StationRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/stations")
public class StationController {

	@Autowired StationRepository stationRepository;
	
	@GetMapping
	public List<StationDto> getAllStations(){
		
		return stationRepository.findAll().stream().map(
				s -> {
					StationDto dto = new StationDto();
					dto.setId(s.getId());
					dto.setName(s.getName());
					dto.setCode(s.getCode());
					return dto;
				}).collect(Collectors.toList());
		
//		findAll() → gets everything at once, as a List<Station>
//		.stream() → sets up to go through that list one at a time
//		.map(...) → for each single Station that comes through, runs your conversion logic and produces one StationDto in its place
//		.collect(...) → gathers all those individual StationDtos back into one final list
	}
	@PostMapping
	public StationDto addStation(@Valid @RequestBody StationCreateDto request) {

		// friendly early exit; the DB unique constraint is the real guarantee
		if (stationRepository.existsByNameIgnoreCase(request.getName())) {
			throw new StationAlreadyExistsException(request.getName());
		}

		Station station = new Station();
		station.setName(request.getName());
		station.setCode(request.getCode());

		Station saved;
		try {
			saved = stationRepository.saveAndFlush(station);
		} catch (DataIntegrityViolationException e) {
			// two simultaneous requests can both pass the check above
			throw new StationAlreadyExistsException(request.getName());
		}

		StationDto dto = new StationDto();
		dto.setId(saved.getId());
		dto.setName(saved.getName());
		dto.setCode(saved.getCode());
		return dto;
	}
	@DeleteMapping("/{id}")
	public void removeStation(@PathVariable Long id) {
		stationRepository.deleteById(id);
	}
	
	
}

