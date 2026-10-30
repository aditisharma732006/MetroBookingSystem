package com.metro.paymentservice.service;

import java.time.LocalDateTime;

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
import com.metro.paymentservice.event.RefundEventPublisher;
import com.metro.paymentservice.exception.EventPublishException;
import com.metro.paymentservice.exception.InvalidPaymentStateException;
import com.metro.paymentservice.exception.InvalidSignatureException;
import com.metro.paymentservice.exception.PaymentGatewayException;
import com.metro.paymentservice.exception.PaymentNotFoundException;
import com.metro.paymentservice.exception.PaymentNotReadyException;
import com.metro.paymentservice.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private RazorpayGateway razorpayGateway;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    @Autowired
    private RefundEventPublisher refundEventPublisher;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    // Keep this below Booking's expiry (15 min). Default 12 if the property is missing.
    @Value("${payment.window-minutes:12}")
    private long paymentWindowMinutes;

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

        // refuse to open Checkout for a booking that is about to expire
        if (payment.getCreatedAt() != null
                && payment.getCreatedAt().plusMinutes(paymentWindowMinutes).isBefore(LocalDateTime.now())) {
            throw new InvalidPaymentStateException("Booking expired, please create a new booking");
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

    // Never blocked by the window: if the money moved, it must be recorded.
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

        // Re-read so we publish what is actually stored
        Payment current = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow();

        if (current.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Payment already " + current.getStatus());
        }

        // If Kafka fails, the row stays SUCCESS with eventPublished=false and the
        // retry scheduler publishes it later. The user still gets 200.
        if (!current.isEventPublished()) {
            try {
                paymentEventPublisher.publishPaymentSuccess(current, current.getRazorpayPaymentId());
                paymentRepository.markEventPublished(current.getId());
            } catch (EventPublishException e) {
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
            log.info("Order {} orphaned for payment {}", orderId, payment.getId());
        }
    }

    // Called by the refund-required consumer. Safe to receive more than once.
    public void refundForBooking(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId).orElse(null);
        if (payment == null) {
            log.error("Refund requested for unknown booking {}", bookingId);
            return;
        }

        // already refunded: make sure Booking was told (idempotent downstream)
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            refundEventPublisher.publishRefundCompleted(bookingId);
            return;
        }

        if (payment.getRazorpayPaymentId() == null) {
            log.error("Refund requested for booking {} but no payment id stored", bookingId);
            return;
        }

        // only one caller wins this update and reaches Razorpay
        int won = paymentRepository.transition(payment.getId(),
                PaymentStatus.SUCCESS, PaymentStatus.REFUND_PENDING);
        if (won == 0) {
            log.info("Refund for booking {} already in progress (status {})", bookingId, payment.getStatus());
            return;
        }

        try {
            razorpayGateway.refund(payment);
        } catch (RuntimeException e) {
            // any failure: put it back so a retry can try again
            paymentRepository.transition(payment.getId(),
                    PaymentStatus.REFUND_PENDING, PaymentStatus.SUCCESS);
            throw e;   // Kafka retries RefundRequired
        }

        paymentRepository.transition(payment.getId(),
                PaymentStatus.REFUND_PENDING, PaymentStatus.REFUNDED);
        log.info("Refund accepted by Razorpay for booking {}", bookingId);

        // if this throws, a redelivered RefundRequired lands in the REFUNDED branch above
        refundEventPublisher.publishRefundCompleted(bookingId);
    }
}