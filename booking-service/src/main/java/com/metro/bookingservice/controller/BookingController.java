package com.metro.bookingservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.metro.bookingservice.dto.request.CreateBookingRequestDto;
import com.metro.bookingservice.dto.response.BookingResponseDto;
import com.metro.bookingservice.service.BookingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/bookings")
public class BookingController {

	@Autowired
	private BookingService bookingService;
	
	@PostMapping
	public ResponseEntity<BookingResponseDto> createBooking(
			@Valid @RequestBody CreateBookingRequestDto request ,
			HttpServletRequest httpRequest){
		Long userId=(Long) httpRequest.getAttribute("userId");
		   if (userId == null) {
		        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		    }
		
		BookingResponseDto response = bookingService.createBooking(request , userId);
		
		return new ResponseEntity<>(response ,HttpStatus.CREATED);
		
	}
	
	@PatchMapping("/{id}/cancel")
	public ResponseEntity<BookingResponseDto> cancelBooking(
	        @PathVariable Long id,
	        HttpServletRequest httpRequest) {

	    Long userId = (Long) httpRequest.getAttribute("userId");
	    if (userId == null) {
	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }

	    BookingResponseDto response = bookingService.cancelBooking(id, userId);
	    return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping
	public ResponseEntity<List<BookingResponseDto>> getMyBookings(HttpServletRequest httpRequest) {

	    Long userId = (Long) httpRequest.getAttribute("userId");
	    if (userId == null) {
	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }

	    List<BookingResponseDto> bookings = bookingService.getMyBookings(userId);
	    return new ResponseEntity<>(bookings, HttpStatus.OK);
	}
	
	
}
