package com.metro.bookingservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.metro.bookingservice.entity.Booking;
import com.metro.bookingservice.entity.BookingStatus;


public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByTicketCode(String ticketCode);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus " +
           "WHERE b.status = :oldStatus AND b.createdAt < :cutoff")
    int expireOldBookings(
            @Param("oldStatus") BookingStatus oldStatus,
            @Param("newStatus") BookingStatus newStatus,
            @Param("cutoff") LocalDateTime cutoff);
    
}
