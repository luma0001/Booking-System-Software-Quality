package com.ekgroup.booking_system.validation;

import com.ekgroup.booking_system.dto.CreateBookingRequest;
import com.ekgroup.booking_system.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Component
public class BookingValidator {

    private final List<java.time.LocalTime> allowedTimeSlots;
    private final Clock clock;

    public BookingValidator(List<java.time.LocalTime> allowedTimeSlots, Clock clock) {
        this.allowedTimeSlots = allowedTimeSlots;
        this.clock = clock;
    }

    public void validateRequest(CreateBookingRequest request) {
        if (request.bookingDate().isBefore(java.time.LocalDate.now(clock))) {
            throw new ValidationException("Booking date must be today or in the future.");
        }
        if (!allowedTimeSlots.contains(request.bookingTime())) {
            throw new ValidationException("Selected time is not an allowed slot.");
        }
    }
}
