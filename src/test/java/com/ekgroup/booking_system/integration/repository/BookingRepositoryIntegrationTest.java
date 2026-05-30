package com.ekgroup.booking_system.integration.repository;

import com.ekgroup.booking_system.model.Activity;
import com.ekgroup.booking_system.model.Booking;
import com.ekgroup.booking_system.model.BookingStatus;
import com.ekgroup.booking_system.model.WeatherRuleType;
import com.ekgroup.booking_system.repository.ActivityRepository;
import com.ekgroup.booking_system.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BookingRepositoryIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        activityRepository.deleteAll();
    }

    @Test
    void TC_INT_REPO_001_countActiveBookingsForSlot_returnsSavedActiveBookingCount() {
        Activity activity = activityRepository.save(new Activity(
                "Quidditch Training",
                "Outdoor broomstick training",
                "Quidditch Pitch",
                10,
                WeatherRuleType.MAX_WIND_SPEED,
                12.0,
                true
        ));

        Booking booking = new Booking();
        booking.setActivity(activity);
        booking.setCustomerName("Harry Potter");
        booking.setCustomerEmail("harry@hogwarts.edu");
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setBookingTime(LocalTime.of(12, 0));
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        long count = bookingRepository.countByActivityAndBookingDateAndBookingTimeAndStatus(
                activity,
                LocalDate.now().plusDays(1),
                LocalTime.of(12, 0),
                BookingStatus.ACTIVE
        );

        assertEquals(1, count);
    }
}
