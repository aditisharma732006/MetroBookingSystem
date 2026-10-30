package com.metro.bookingservice.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.metro.bookingservice.entity.Booking;
import com.metro.bookingservice.entity.BookingStatus;
import com.metro.bookingservice.event.RefundEventPublisher;
import com.metro.bookingservice.exception.EventPublishException;
import com.metro.bookingservice.repository.BookingRepository;

@Component
public class RefundRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefundRetryScheduler.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RefundEventPublisher refundEventPublisher;

    // every 5 minutes by default
    @Scheduled(fixedDelayString = "${booking.refund-retry.rate-ms:300000}")
    public void retryRefunds() {
        for (Booking b : bookingRepository.findByStatus(BookingStatus.PAID_AFTER_EXPIRY)) {
            try {
                refundEventPublisher.publishRefundRequired(b.getId(), "BOOKING_EXPIRED_AFTER_PAYMENT");
            } catch (EventPublishException e) {
                log.warn("Kafka unavailable, refund retry deferred");
                break;
            } catch (RuntimeException e) {
                log.error("Refund retry failed for booking {}", b.getId(), e);
            }
        }
    }
}