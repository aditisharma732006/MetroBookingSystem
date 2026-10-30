package com.metro.paymentservice.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.metro.paymentservice.entity.Payment;
import com.metro.paymentservice.entity.PaymentStatus;
import com.metro.paymentservice.event.PaymentEventPublisher;
import com.metro.paymentservice.exception.EventPublishException;
import com.metro.paymentservice.repository.PaymentRepository;

@Component
public class PaymentEventRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventRetryScheduler.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    @Scheduled(fixedDelayString = "${payment.event-retry.rate-ms:10000}")
    public void republishUnpublished() {
        List<Payment> pending =
                paymentRepository.findByStatusAndEventPublishedFalse(PaymentStatus.SUCCESS);

        for (Payment payment : pending) {
            try {
                paymentEventPublisher.publishPaymentSuccess(payment, payment.getRazorpayPaymentId());
                paymentRepository.markEventPublished(payment.getId());
                log.info("Republished PaymentSuccess for booking {}", payment.getBookingId());
            } catch (EventPublishException e) {
                log.warn("Kafka still unavailable, will retry later");
                break;   // no point trying the rest while Kafka is down
            }
        }
    }
}