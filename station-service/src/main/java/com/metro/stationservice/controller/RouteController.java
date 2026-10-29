package com.metro.stationservice.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
// REMOVED: import java.util.stream.Collectors (unused, and you avoid streams)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.metro.stationservice.dto.response.LineDto;
import com.metro.stationservice.dto.response.PathStepDto;
import com.metro.stationservice.dto.response.RouteResponseDto;
import com.metro.stationservice.dto.response.StationDto;
import com.metro.stationservice.entity.Station;
import com.metro.stationservice.exception.RouteNotReachableException; // NEW import
import com.metro.stationservice.exception.SameStationException;
import com.metro.stationservice.repository.StationRepository;
import com.metro.stationservice.service.RouteService;
import com.metro.stationservice.util.RouteResult; // NEW import
// REMOVED: LineRepository import and field (it was never used in this controller)

@RestController
@RequestMapping("/route")
public class RouteController {

	@Autowired
	private RouteService routeService; // CHANGED: public -> private

	@Autowired
	private StationRepository stationRepository; // CHANGED: public -> private

	@GetMapping
	public RouteResponseDto getRoute(@RequestParam Long from, @RequestParam Long to) {
		if (from.equals(to)) {
			throw new SameStationException();
		}

		// CHANGED: capture the returned result instead of relying on service fields
		RouteResult result = routeService.runDijkstra(from);

		// CHANGED: read distance from the result, and guard the two failure cases:
		// null = 'to' is not in the graph at all, MAX_VALUE = in the graph but not
		// connected to 'from'
		Double distance = result.getDistances().get(to);
		if (distance == null || distance.doubleValue() == Double.MAX_VALUE) {
			throw new RouteNotReachableException(from, to);
		}
		double totalDistance = distance.doubleValue();

		// CHANGED: getPath now takes the result
		List<Long> pathIds = routeService.getPath(result, to);

		// UNCHANGED: fare stays BigDecimal
		BigDecimal fare = routeService.calculateFare(totalDistance);

		List<PathStepDto> path = new ArrayList<PathStepDto>();

		for (Long stationId : pathIds) {
			Station station = stationRepository.findById(stationId).orElseThrow();

			StationDto stationDto = new StationDto();
			stationDto.setId(station.getId());
			stationDto.setName(station.getName());
			stationDto.setCode(station.getCode());

			// CHANGED: read from result.getLineUsedToReach() instead of
			// routeService.lineUsedToReach
			Long lineId = result.getLineUsedToReach().get(stationId);
			LineDto lineDto = (lineId != null) ? routeService.getLineDto(lineId) : null;

			PathStepDto pathStepDto = new PathStepDto();
			pathStepDto.setStation(stationDto);
			pathStepDto.setLine(lineDto);

			path.add(pathStepDto);
		}

		RouteResponseDto response = new RouteResponseDto();
		response.setPath(path);
		response.setTotalDistanceKm(totalDistance);
		response.setFare(fare);

		return response;
	}
}