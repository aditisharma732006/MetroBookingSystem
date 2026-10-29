package com.metro.bookingservice.scheduler;

import com.metro.bookingservice.entity.Booking;
import com.metro.bookingservice.entity.BookingStatus;
import com.metro.bookingservice.repository.BookingRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingExpiryScheduler {

    @Autowired
    private BookingRepository bookingRepository;

    @Value("${booking.expiry.minutes}")
    private int expiryMinutes;

    @Scheduled(fixedDelayString = "${booking.expiry.check-rate-ms}")
    @Transactional
    public void expireOldBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expiryMinutes);

        //this version is not good .save is query task so if there are 100 booking need to expired, there will be 500 quesry for that
        //instead we use single bulk query like change 500 row at once
//        List<Booking> expiredCandidates =
//                bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);
//
//        for (Booking booking : expiredCandidates) {
//            booking.setStatus(BookingStatus.EXPIRED);
//            bookingRepository.save(booking);
//        }
        bookingRepository.expireOldBookings(BookingStatus.PENDING, BookingStatus.EXPIRED, cutoff);
        
        
        
    }
}