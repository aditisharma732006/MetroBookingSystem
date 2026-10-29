package com.metro.bookingservice.event;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.metro.bookingservice.entity.Booking;
import com.metro.bookingservice.exception.EventPublishException;

@Component
public class BookingEventPublisher {

    private static final String TOPIC = "booking-created";

    //Spring Kafka’s tool for sending
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingCreated(Booking booking) {
        BookingCreatedEvent event = new BookingCreatedEvent();
        
        //A unique id for this event. Payment later records it to recognise “I’ve already processed this event” - idempotency
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("BOOKING_CREATED");
        event.setOccurredAt(Instant.now());
        event.setBookingId(booking.getId());
        event.setUserId(booking.getUserId());
        event.setFare(booking.getFare());
        event.setTicketCode(booking.getTicketCode());

        String key = String.valueOf(booking.getId());

        try {
            kafkaTemplate.send(TOPIC, key, event).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) { //: your thread was asked to stop while it was waiting, for example the server is shutting down. It’s rare.
        	
        	//, Thread.currentThread().interrupt(), puts the “I was interrupted” flag back. Catching InterruptedException clears that flag, 
        	// and other code, such as Tomcat during shutdown, may need it.
            Thread.currentThread().interrupt();
            throw new EventPublishException("Could not start payment, please try again");
        } catch (ExecutionException | TimeoutException e) {
            throw new EventPublishException("Could not start payment, please try again");
        }
    }
}