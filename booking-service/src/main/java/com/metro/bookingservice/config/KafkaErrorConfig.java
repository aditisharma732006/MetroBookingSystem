package com.metro.bookingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(6);
        backOff.setInitialInterval(1000);   // 1s, 2s, 4s, 8s, 16s, 30s
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30000);
        return new DefaultErrorHandler(backOff);
    }
}