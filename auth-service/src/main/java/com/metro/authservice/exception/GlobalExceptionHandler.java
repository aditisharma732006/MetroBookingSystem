package com.metro.authservice.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Map<String, String>> HandleEmailExists(EmailAlreadyExistsException ex){
		return buildResponse(ex.getMessage(), HttpStatus.CONFLICT); //409 - incoming data was all correct but its already exists
		
	}
	
	@ExceptionHandler(InvalidVerificationTokenException.class)
	public ResponseEntity<Map<String, String>> HandleVerification(InvalidVerificationTokenException ex){
		return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
	    return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
	}
	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<Map<String, String>> handleEmailNotVerified(EmailNotVerifiedException ex) {
	    return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
	}
	// NEW
	@ExceptionHandler(EmailSendFailedException.class)
	public ResponseEntity<Map<String, String>> handleEmailSendFailed(EmailSendFailedException ex) {
	    return buildResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String , String>> HandleValidationErrors(MethodArgumentNotValidException ex){
		Map<String , String> errors = new HashMap<>();
		for(FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}
		return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
	}
	
	public ResponseEntity<Map<String,String>> buildResponse(String message , HttpStatus status){
		Map<String , String> error=new HashMap<>();
		error.put("error", message);
		error.put("status", status.toString());
		return new ResponseEntity<>(error,status);
	
	}
	

}
