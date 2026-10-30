package com.metro.bookingservice.event;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.metro.bookingservice.exception.EventPublishException;

@Component
public class RefundEventPublisher {

    private static final String TOPIC = "refund-required";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRefundRequired(Long bookingId, String reason) {
        RefundRequiredEvent event = new RefundRequiredEvent();
        event.setEventId("REFUND_REQUIRED-" + bookingId);   // same id on every repeat
        event.setEventType("REFUND_REQUIRED");
        event.setOccurredAt(Instant.now());
        event.setBookingId(bookingId);
        event.setReason(reason);

        try {
            kafkaTemplate.send(TOPIC, String.valueOf(bookingId), event).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Could not publish RefundRequired event");
        } catch (ExecutionException | TimeoutException | KafkaException e) {
            throw new EventPublishException("Could not publish RefundRequired event");
        }
    }
}