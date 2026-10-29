package com.metro.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {
	
	@NotBlank(message = "name is Required")
	@Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only letters and spaces")
	private String name;
	
	@NotBlank(message = "email is required")
	@Email(message = "Email format is invalid")
	private String email;
	
	@NotBlank(message = "password is required")
	@Size(min=6 , message = "Password must be at least 6 characters")
	@Pattern(
	        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[_\\-@]).{6,}$",
	        message = "Password must be at least 6 characters and include a letter, a number, and one of _ - @"
	    )
	private String password;
}
