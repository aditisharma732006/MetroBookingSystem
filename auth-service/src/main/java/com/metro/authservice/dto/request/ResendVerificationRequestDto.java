package com.metro.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// NEW: body for POST /auth/resend-verification
@Getter
@Setter
public class ResendVerificationRequestDto {

	@NotBlank(message = "Email is required")
	@Email(message = "Email format is invalid")
	private String email;
}