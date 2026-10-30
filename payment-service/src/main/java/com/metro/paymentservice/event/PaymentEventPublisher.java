package com.metro.paymentservice.event;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.metro.paymentservice.entity.Payment;
import com.metro.paymentservice.exception.EventPublishException;

@Component
public class PaymentEventPublisher {

    private static final String TOPIC = "payment-success";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSuccess(Payment payment, String razorpayPaymentId) {
        PaymentSuccessEvent event = new PaymentSuccessEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("PAYMENT_SUCCESS");
        event.setOccurredAt(Instant.now());
        event.setBookingId(payment.getBookingId());
        event.setUserId(payment.getUserId());
        event.setAmount(payment.getAmount());
        event.setRazorpayPaymentId(razorpayPaymentId);

        String key = String.valueOf(payment.getBookingId());

        try {
            kafkaTemplate.send(TOPIC, key, event).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Payment recorded, confirmation pending. Please retry.");
        } catch (ExecutionException | TimeoutException e) {
            throw new EventPublishException("Payment recorded, confirmation pending. Please retry.");
        }
    }
}