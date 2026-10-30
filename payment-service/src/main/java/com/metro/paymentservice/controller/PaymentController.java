package com.metro.paymentservice.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.metro.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.metro.paymentservice.dto.response.PaymentOrderResponseDto;
import com.metro.paymentservice.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentOrderResponseDto> getPaymentForBooking(
            @PathVariable Long bookingId, HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(paymentService.getPaymentForBooking(bookingId, userId));
    }
    
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(
            @Valid @RequestBody VerifyPaymentRequestDto request, HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        paymentService.verifyPayment(request, userId);
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }
}