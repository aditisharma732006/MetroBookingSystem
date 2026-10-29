package com.metro.stationservice.controller;

import com.metro.stationservice.dto.response.LineDto;
import com.metro.stationservice.dto.request.LineCreateDto;
import com.metro.stationservice.entity.Line;
import com.metro.stationservice.repository.LineRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/lines")
public class LineController {

    @Autowired
    private LineRepository lineRepository;

    @GetMapping
    public List<LineDto> getAllLines() {
        List<Line> lines = lineRepository.findAll();
        List<LineDto> result = new ArrayList<>();

        for (Line line : lines) {
            LineDto dto = new LineDto();
            dto.setId(line.getId());
            dto.setName(line.getName());
            dto.setColor(line.getColor());
            result.add(dto);
        }

        return result;
    }

    @PostMapping
    public LineDto addLine(@Valid @RequestBody LineCreateDto request) {
        Line line = new Line();
        line.setName(request.getName());
        line.setColor(request.getColor());
        Line saved = lineRepository.save(line);

        LineDto dto = new LineDto();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setColor(saved.getColor());
        return dto;
    }

    @DeleteMapping("/{id}")
    public void deleteLine(@PathVariable Long id) {
        lineRepository.deleteById(id);
    }
}