
package com.metro.stationservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.metro.stationservice.dto.response.LineDto;
import com.metro.stationservice.entity.FareSlab;
import com.metro.stationservice.entity.Line;
import com.metro.stationservice.entity.Segment;
import com.metro.stationservice.exception.FareNotFoundException;
import com.metro.stationservice.exception.LineNotFoundException;
import com.metro.stationservice.repository.FareSlabRepository;
import com.metro.stationservice.repository.LineRepository;
import com.metro.stationservice.repository.SegmentRepository;
import com.metro.stationservice.util.Edge;
import com.metro.stationservice.util.RouteResult; // NEW import

@Service
public class RouteService {

	@Autowired
	private SegmentRepository segmentRepository;

	@Autowired
	private FareSlabRepository fareSlabRepository;

	@Autowired
	private LineRepository lineRepository;

	// REMOVED: the three public fields (distances, previousStation,
	// lineUsedToReach).
	// They were per-request data stored in a singleton (not thread-safe), and the
	// controller read them through a proxy, which gave null. They are now local
	// variables inside runDijkstra and are returned in a RouteResult.

	public Map<Long, List<Edge>> buildAdjacencyList() {
		List<Segment> segments = segmentRepository.findAll();

		Map<Long, List<Edge>> adjList = new HashMap<Long, List<Edge>>();

		for (Segment segment : segments) {
			Long stationAId = segment.getStationA().getId();
			Long stationBId = segment.getStationB().getId();
			double distance = segment.getDistanceKm();
			Long lineId = segment.getLine().getId();

			// CHANGED: replaced the computeIfAbsent lambda with a plain if (your no-lambda
			// rule)
			if (adjList.get(stationAId) == null) {
				adjList.put(stationAId, new ArrayList<Edge>());
			}
			adjList.get(stationAId).add(new Edge(stationBId, distance, lineId));

			// undirected graph - add the connection BOTH ways
			if (adjList.get(stationBId) == null) {
				adjList.put(stationBId, new ArrayList<Edge>());
			}
			adjList.get(stationBId).add(new Edge(stationAId, distance, lineId));
		}
		return adjList;
	}

	// CHANGED: return type void -> RouteResult
	public RouteResult runDijkstra(Long sourceStationId) {
		Map<Long, List<Edge>> adjList = buildAdjacencyList();

		// CHANGED: these are now LOCAL variables (each request gets its own maps).
		// 'final' on distances because the anonymous Comparator below reads it.

		// AFTER runDijkstra() finishes - getPath() and fare lookup need them later
		// final answer: shortest distance to each station
		// helper: "I reached this station FROM which station"
		// final answer: which line was used to arrive at each station
		final Map<Long, Double> distances = new HashMap<Long, Double>();
		Map<Long, Long> previousStation = new HashMap<Long, Long>();
		Map<Long, Long> lineUsedToReach = new HashMap<Long, Long>();

		for (Long stationId : adjList.keySet()) {
			distances.put(stationId, Double.MAX_VALUE);
		}
		distances.put(sourceStationId, 0.0);

		PriorityQueue<Long> pq = new PriorityQueue<Long>(new Comparator<Long>() {
			public int compare(Long a, Long b) {
				// compare by DISTANCE, not by station ID
				return Double.compare(distances.get(a), distances.get(b));
			}
		});

		pq.add(sourceStationId);
		Set<Long> visited = new HashSet<Long>();

		while (!pq.isEmpty()) {
			Long current = pq.poll(); // closest unvisited station right now

			// stale entries can exist in pq - skip them
			if (visited.contains(current))
				continue;
			visited.add(current); // lock in current's distance

			for (Edge edge : adjList.getOrDefault(current, new ArrayList<Edge>())) {
				Long neighborId = edge.getNeighborStationId();

				double newDist = distances.get(current) + edge.getDistanceKm();

				if (newDist < distances.get(neighborId)) {
					distances.put(neighborId, newDist);
					previousStation.put(neighborId, current);
					lineUsedToReach.put(neighborId, edge.getLineId());
					pq.add(neighborId);
				}
			}
		}

		// NEW: package the three maps and return them
		RouteResult result = new RouteResult();
		result.setDistances(distances);
		result.setPreviousStation(previousStation);
		result.setLineUsedToReach(lineUsedToReach);
		return result;

	}

	// CHANGED: now takes the RouteResult as a parameter (it used the old field
	// before)
	public List<Long> getPath(RouteResult result, Long destinationId) {
		List<Long> path = new ArrayList<Long>();
		Long current = destinationId;

		// walk backward (destination -> ... -> source); the source has no entry, so
		// current becomes null
		while (current != null) {
			path.add(0, current);
			current = result.getPreviousStation().get(current); // CHANGED: reads from result
		}
		return path;
	}

	// UNCHANGED: fare is still BigDecimal (slab.getFare() is returned as is)
	public BigDecimal calculateFare(double distanceKm) {
		List<FareSlab> allSlab = fareSlabRepository.findAll();

		for (FareSlab slab : allSlab) {
			if (distanceKm >= slab.getMinDistanceKm() && distanceKm <= slab.getMaxDistanceKm()) {
				return slab.getFare();
			}
		}
		throw new FareNotFoundException(distanceKm);
	}

	public LineDto getLineDto(long lineId) {
		// CHANGED: replaced the orElseThrow lambda with a plain null check (no-lambda
		// rule)
		Line line = lineRepository.findById(lineId).orElse(null);
		if (line == null) {
			throw new LineNotFoundException(lineId);
		}
		LineDto dto = new LineDto();
		dto.setId(line.getId());
		dto.setName(line.getName());
		dto.setColor(line.getColor());

		return dto;
	}
}
