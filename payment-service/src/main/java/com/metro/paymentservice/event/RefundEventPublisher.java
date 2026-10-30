package com.metro.paymentservice.event;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.metro.paymentservice.exception.EventPublishException;

@Component
public class RefundEventPublisher {

    private static final String TOPIC = "refund-completed";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRefundCompleted(Long bookingId) {
        RefundCompletedEvent event = new RefundCompletedEvent();
        event.setEventId("REFUND_COMPLETED-" + bookingId);
        event.setEventType("REFUND_COMPLETED");
        event.setOccurredAt(Instant.now());
        event.setBookingId(bookingId);

        try {
            kafkaTemplate.send(TOPIC, String.valueOf(bookingId), event).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Could not publish RefundCompleted event");
        } catch (ExecutionException | TimeoutException | KafkaException e) {
            throw new EventPublishException("Could not publish RefundCompleted event");
        }
    }
}