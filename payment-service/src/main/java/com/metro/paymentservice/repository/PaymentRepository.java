package com.metro.paymentservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.metro.paymentservice.entity.Payment;
import com.metro.paymentservice.entity.PaymentStatus;

import jakarta.transaction.Transactional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByBookingId(Long bookingId);

    Optional<Payment> findByBookingId(Long bookingId);          // no annotations

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);   // no annotations

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Payment p SET p.razorpayOrderId = :orderId " +
           "WHERE p.id = :id AND p.razorpayOrderId IS NULL")
    int attachOrderId(@Param("id") Long id, @Param("orderId") String orderId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Payment p SET p.status = :newStatus, p.razorpayPaymentId = :paymentId " +
           "WHERE p.id = :id AND p.status = :oldStatus")
    int markStatus(@Param("id") Long id,
                   @Param("paymentId") String paymentId,
                   @Param("oldStatus") PaymentStatus oldStatus,
                   @Param("newStatus") PaymentStatus newStatus);
    
    List<Payment> findByStatusAndEventPublishedFalse(PaymentStatus status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Payment p SET p.eventPublished = true WHERE p.id = :id")
    int markEventPublished(@Param("id") Long id);
}