package com.metro.paymentservice.event;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.metro.paymentservice.service.PaymentService;

@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    @Autowired
    private PaymentService paymentService;

    @Value("${payment.max-event-age-minutes}")
    private long maxEventAgeMinutes;

    @KafkaListener(topics = "booking-created")
    public void onBookingCreated(BookingCreatedEvent event) {

        if (event == null || event.getBookingId() == null || event.getFare() == null
                || event.getUserId() == null || event.getOccurredAt() == null) {
            log.warn("Skipping malformed BookingCreated event: {}", event == null ? "null" : event.getEventId());
            return;
        }

        long ageMinutes = Duration.between(event.getOccurredAt(), Instant.now()).toMinutes();
        if (ageMinutes > maxEventAgeMinutes) {
            log.info("Skipping stale event for booking {} (age {} min)", event.getBookingId(), ageMinutes);
            return;
        }

        try {
            boolean created = paymentService.createPaymentForBooking(event);
            if (created) {
                log.info("Payment created for booking {}", event.getBookingId());
            } else {
                log.info("Duplicate event for booking {}, ignored", event.getBookingId());
            }
        } catch (DataIntegrityViolationException e) {
            // two deliveries raced past existsByBookingId; the unique constraint caught it
            log.info("Duplicate event for booking {} caught by DB constraint", event.getBookingId());
        }
    }
}