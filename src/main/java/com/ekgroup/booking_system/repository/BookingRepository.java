package com.ekgroup.booking_system.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ekgroup.booking_system.model.Activity;
import com.ekgroup.booking_system.model.Booking;
import com.ekgroup.booking_system.model.BookingStatus;


public interface BookingRepository extends JpaRepository<Booking, UUID> {

    long countByActivityAndBookingDateAndBookingTimeAndStatus(
            Activity activity,
            LocalDate bookingDate,
            LocalTime bookingTime,
            BookingStatus status
    );

    boolean existsByCustomerEmailAndActivityAndBookingDateAndBookingTimeAndStatus(
            String customerEmail,
            Activity activity,
            LocalDate bookingDate,
            LocalTime bookingTime,
            BookingStatus status
    );

    Optional<Booking> findByIdAndStatus(UUID id, BookingStatus status);

    @Query(""" 
        select b.bookingTime as bookingTime, count(b) as total 
        from Booking b 
        where b.activity = :activity
        and b.bookingDate = :bookingDate
        and b.status = :status
        group by b.bookingTime
""")
    List<SlotCountProjection> countActiveBookingsPerSlot(
        @Param("activity") Activity activity,
        @Param("bookingDate") LocalDate bookingDate,
        @Param("status") BookingStatus bookingStatus
    );



}

