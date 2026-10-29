package com.metro.bookingservice.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Retryer;


public class StationServiceFeignConfig {

	@Bean
	public Retryer retryer() {
		 // (period, maxPeriod, maxAttempts)
//		Feign increases the wait time between each retry (exponential backoff — don’t hammer a struggling service immediately, give it breathing room), capped at 2000ms.
		//error-the timeout and all retry is too much and against the gateway means 3 + 0.5 + 3 + 0.75 + 3 ≈ 10s, against the Gateway’s 5s. The Gateway tells the user “503” while Booking finishes later and saves the booking
		//so we need small numbers
		return new Retryer.Default(500,2000,2);
	}
}
