package com.metro.authservice.exception;

// NEW: thrown when the verification email could not be sent (mapped to 503)
public class EmailSendFailedException extends RuntimeException {
	public EmailSendFailedException() {
		super("Could not send the verification email. Please try again in a moment.");
	}
}