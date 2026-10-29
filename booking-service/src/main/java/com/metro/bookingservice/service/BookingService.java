package com.metro.bookingservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.metro.bookingservice.client.StationServiceClient;
import com.metro.bookingservice.dto.request.CreateBookingRequestDto;
import com.metro.bookingservice.dto.response.BookingResponseDto;
import com.metro.bookingservice.dto.response.PathStepDto;
import com.metro.bookingservice.dto.response.RouteResponseDto;
import com.metro.bookingservice.entity.Booking;
import com.metro.bookingservice.entity.BookingStatus;
import com.metro.bookingservice.event.BookingEventPublisher;
import com.metro.bookingservice.exception.BookingNotFoundException;
import com.metro.bookingservice.exception.InvalidBookingRequestException;
import com.metro.bookingservice.exception.InvalidBookingStatusException;
import com.metro.bookingservice.exception.RouteNotFoundException;
import com.metro.bookingservice.exception.StationServiceUnavailableException;
import com.metro.bookingservice.repository.BookingRepository;

import feign.FeignException;

@Service
public class BookingService {
	@Autowired
	public StationServiceClient stationServiceClient;

	@Autowired
	public BookingRepository bookingRepository;
	
	@Autowired
	private BookingEventPublisher bookingEventPublisher;
	
	public BookingResponseDto createBooking(CreateBookingRequestDto request , Long userId) {
		if (request.getFromStationId().equals(request.getToStationId())) {
		    throw new InvalidBookingRequestException("Source and destination stations must be different");
		}
		RouteResponseDto route;
		try {
		    route = stationServiceClient.getRoute(request.getFromStationId(), request.getToStationId());
		} catch (FeignException e) {
		    if (e.status() >= 400 && e.status() < 500) {
		        throw new RouteNotFoundException("No route found for the given stations");
		    }
		    throw new StationServiceUnavailableException("Station service is currently unavailable");
		}
		
		Booking booking = new Booking();
		booking.setUserId(userId);
		booking.setFromStationId(request.getFromStationId());
		booking.setToStationId(request.getToStationId());
        booking.setFromStationName(extractStationName(route, true));
        booking.setToStationName(extractStationName(route, false));
        booking.setFare(route.getFare());
        booking.setDistanceKm(route.getTotalDistanceKm());
        booking.setStatus(BookingStatus.PENDING);
        
        Booking saved = bookingRepository.save(booking);
        
        bookingEventPublisher.publishBookingCreated(saved);   // after the save, outside any transaction

        
        return toResponseDto(saved);
	}
	
	private String extractStationName(RouteResponseDto route, boolean isFrom) {
	    if (route.getPath() == null || route.getPath().isEmpty()) {
	        throw new RouteNotFoundException("Route data is incomplete for the given stations");
	    }
	    PathStepDto step = isFrom ? route.getPath().get(0) : route.getPath().get(route.getPath().size() - 1);
	    return step.getStation().getName();
	}
	
    public BookingResponseDto cancelBooking(Long bookingId , Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BookingNotFoundException("Booking not found");
        }
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStatusException(
                    "Only PENDING bookings can be cancelled. Current status: " + booking.getStatus());
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        
        return toResponseDto(saved);
    }
    
    public List<BookingResponseDto> getMyBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<BookingResponseDto> result = new ArrayList<>();
        for (Booking booking : bookings) {
            result.add(toResponseDto(booking));
        }
        return result;
    }
	
    private BookingResponseDto toResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setTicketCode(booking.getTicketCode());
        dto.setFromStationName(booking.getFromStationName());
        dto.setToStationName(booking.getToStationName());
        dto.setFare(booking.getFare());
        dto.setDistanceKm(booking.getDistanceKm());
        dto.setStatus(booking.getStatus().name());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }
    

    
}
