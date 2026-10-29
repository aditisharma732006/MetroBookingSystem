package com.metro.bookingservice.client;

import com.metro.bookingservice.dto.response.RouteResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "STATION-SERVICE", configuration = StationServiceFeignConfig.class)
public interface StationServiceClient {

    @GetMapping("/route")
    RouteResponseDto getRoute(@RequestParam("from") Long from, @RequestParam("to") Long to);
}