package com.metro.authservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.metro.authservice.dto.request.LoginRequestDto;
import com.metro.authservice.dto.request.RegisterRequestDto;
import com.metro.authservice.dto.request.ResendVerificationRequestDto; // NEW import
import com.metro.authservice.dto.response.RegisterResponseDto;
import com.metro.authservice.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/register")
	public RegisterResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
		RegisterResponseDto dto = authService.register(request);
		return dto;
	}

	@GetMapping("/verify")
	public String verify(@RequestParam String token) {
		return authService.verifyEmail(token);
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request) {
		String token = authService.login(request);

		Map<String, String> response = new HashMap<String, String>();
		response.put("token", token);

		return ResponseEntity.ok(response);
	}

	// NEW: for users whose verification email was lost or failed
	@PostMapping("/resend-verification")
	public ResponseEntity<Map<String, String>> resendVerification(
			@Valid @RequestBody ResendVerificationRequestDto request) {
		authService.resendVerification(request.getEmail());

		Map<String, String> response = new HashMap<String, String>();
		// same message in every case, so it does not reveal whether the email exists
		response.put("message", "If this email is registered and not yet verified, a new email has been sent.");
		return ResponseEntity.ok(response);
	}
}