package com.metro.stationservice.controller;

import com.metro.stationservice.dto.response.FareSlabDto;
import com.metro.stationservice.dto.request.FareSlabCreateDto;
import com.metro.stationservice.entity.FareSlab;
import com.metro.stationservice.repository.FareSlabRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/fare-slabs")
public class FareSlabController {

    @Autowired
    private FareSlabRepository fareSlabRepository;

    @GetMapping
    public List<FareSlabDto> getAllFareSlabs() {
        List<FareSlab> slabs = fareSlabRepository.findAll();
        List<FareSlabDto> result = new ArrayList<>();

        for (FareSlab slab : slabs) {
            FareSlabDto dto = new FareSlabDto();
            dto.setId(slab.getId());
            dto.setMinDistanceKm(slab.getMinDistanceKm());
            dto.setMaxDistanceKm(slab.getMaxDistanceKm());
            dto.setFare(slab.getFare());
            result.add(dto);
        }

        return result;
    }

    @PostMapping
    public FareSlabDto addFareSlab(@Valid @RequestBody FareSlabCreateDto request) {
        FareSlab slab = new FareSlab();
        slab.setMinDistanceKm(request.getMinDistanceKm());
        slab.setMaxDistanceKm(request.getMaxDistanceKm());
        slab.setFare(request.getFare());
        FareSlab saved = fareSlabRepository.save(slab);

        FareSlabDto dto = new FareSlabDto();
        dto.setId(saved.getId());
        dto.setMinDistanceKm(saved.getMinDistanceKm());
        dto.setMaxDistanceKm(saved.getMaxDistanceKm());
        dto.setFare(saved.getFare());
        return dto;
    }

    @DeleteMapping("/{id}")
    public void deleteFareSlab(@PathVariable Long id) {
        fareSlabRepository.deleteById(id);
    }
}