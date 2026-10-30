package com.metro.paymentservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.metro.paymentservice.client.RazorpayGateway;
import com.metro.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.metro.paymentservice.dto.response.PaymentOrderResponseDto;
import com.metro.paymentservice.entity.Payment;
import com.metro.paymentservice.entity.PaymentStatus;
import com.metro.paymentservice.event.BookingCreatedEvent;
import com.metro.paymentservice.event.PaymentEventPublisher;
import com.metro.paymentservice.exception.EventPublishException;
import com.metro.paymentservice.exception.InvalidPaymentStateException;
import com.metro.paymentservice.exception.InvalidSignatureException;
import com.metro.paymentservice.exception.PaymentGatewayException;
import com.metro.paymentservice.exception.PaymentNotFoundException;
import com.metro.paymentservice.exception.PaymentNotReadyException;
import com.metro.paymentservice.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private RazorpayGateway razorpayGateway;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // Returns true if a new payment row was created, false if one already existed.
    public boolean createPaymentForBooking(BookingCreatedEvent event) {
        boolean isNew = false;

        Payment payment = paymentRepository.findByBookingId(event.getBookingId()).orElse(null);

        if (payment == null) {
            Payment fresh = new Payment();
            fresh.setBookingId(event.getBookingId());
            fresh.setUserId(event.getUserId());
            fresh.setAmount(event.getFare());          // from the event, never from a client
            fresh.setStatus(PaymentStatus.CREATED);
            try {
                payment = paymentRepository.save(fresh);
                isNew = true;
            } catch (DataIntegrityViolationException e) {
                // a concurrent delivery inserted it first; use that row
                payment = paymentRepository.findByBookingId(event.getBookingId()).orElseThrow();
            }
        }

        ensureRazorpayOrder(payment);   // no-op if it already has one
        return isNew;
    }

    public PaymentOrderResponseDto getPaymentForBooking(Long bookingId, Long userId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotReadyException("Payment not ready yet, please retry"));

        if (!payment.getUserId().equals(userId)) {
            throw new PaymentNotFoundException("Payment not found");
        }

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new InvalidPaymentStateException("Payment already " + payment.getStatus());
        }

        if (payment.getRazorpayOrderId() == null) {
            ensureRazorpayOrder(payment);   // throws PaymentGatewayException if Razorpay fails
            payment = paymentRepository.findByBookingId(bookingId).orElseThrow();   // re-read: the winner's id
        }

        if (payment.getRazorpayOrderId() == null) {
            throw new PaymentGatewayException("Order could not be created", null);
        }

        PaymentOrderResponseDto dto = new PaymentOrderResponseDto();
        dto.setBookingId(payment.getBookingId());
        dto.setRazorpayOrderId(payment.getRazorpayOrderId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency("INR");
        dto.setKeyId(razorpayKeyId);
        return dto;
    }

    public void verifyPayment(VerifyPaymentRequestDto req, Long userId) {
        Payment payment = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (!payment.getUserId().equals(userId)) {
            throw new PaymentNotFoundException("Payment not found");
        }

        if (!razorpayGateway.verifySignature(req.getRazorpayOrderId(),
                req.getRazorpayPaymentId(), req.getRazorpaySignature())) {
            throw new InvalidSignatureException("Payment verification failed");
        }

        if (payment.getStatus() == PaymentStatus.CREATED) {
            paymentRepository.markStatus(payment.getId(), req.getRazorpayPaymentId(),
                    PaymentStatus.CREATED, PaymentStatus.SUCCESS);
        } else if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Payment already " + payment.getStatus());
        }

        // Re-read so we publish what is actually stored (also covers a racing duplicate call)
        Payment current = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow();

        if (current.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Payment already " + current.getStatus());
        }

        // Publish only if not already published. If Kafka fails here, the row stays
        // SUCCESS with eventPublished=false and the retry scheduler finishes the job.
        if (!current.isEventPublished()) {
            try {
                paymentEventPublisher.publishPaymentSuccess(current, current.getRazorpayPaymentId());
                paymentRepository.markEventPublished(current.getId());
            } catch (EventPublishException e) {
                // payment is already SUCCESS in the DB; the scheduler will publish it shortly
                log.warn("PaymentSuccess publish deferred for booking {}, scheduler will retry",
                        current.getBookingId());
            }
        }
    }

    public void ensureRazorpayOrder(Payment payment) {
        if (payment.getRazorpayOrderId() != null) {
            return;
        }
        String orderId = razorpayGateway.createOrder(payment);
        int updated = paymentRepository.attachOrderId(payment.getId(), orderId);
        if (updated == 0) {
            // someone else attached an order first; ours is an unpaid orphan, harmless
            log.info("Order {} orphaned for payment {}", orderId, payment.getId());
        }
    }
}