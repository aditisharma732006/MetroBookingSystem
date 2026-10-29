package com.metro.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${app.gateway.base-url}")
	private String gatewayBaseUrl;
	
	public void sendVerificationEmail(String toEmail , String token) {
		String verificationLink = gatewayBaseUrl + "/auth/verify?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("noreply@metrobooking.com");
		message.setTo(toEmail);
		message.setSubject("Verify your Metro Booking account");
		message.setText("Click the link to verify your email: " + verificationLink);
		
		mailSender.send(message);
	}
}
