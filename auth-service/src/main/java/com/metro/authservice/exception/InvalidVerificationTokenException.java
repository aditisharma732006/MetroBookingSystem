package com.metro.authservice.exception;

public class InvalidVerificationTokenException extends RuntimeException {
	public InvalidVerificationTokenException() {
		super("Invalid or expired verification token");
	}
}
