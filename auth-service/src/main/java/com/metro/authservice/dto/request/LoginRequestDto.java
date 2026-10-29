package com.metro.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// CHANGED: @Data replaced with @Getter @Setter (your rule for request DTOs)
@Getter
@Setter
public class LoginRequestDto {

	@NotBlank(message = "Email is required")
	@Email(message = "Email format is invalid")
	private String email;

	@NotBlank(message = "Password is required")
	private String password;
}