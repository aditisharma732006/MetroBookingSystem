package com.metro.bookingservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.metro.bookingservice.service.BookingService;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    @Autowired
    private BookingService bookingService;

    @KafkaListener(topics = "payment-success")
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        if (event == null || event.getBookingId() == null
                || event.getUserId() == null || event.getAmount() == null) {
            log.warn("Skipping malformed PaymentSuccess event");
            return;
        }
        // real failures (DB down, Kafka down on the refund publish) propagate so Kafka retries
        bookingService.handlePaymentSuccess(event);
    }
}