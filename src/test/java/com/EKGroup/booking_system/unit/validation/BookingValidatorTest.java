package com.ekgroup.booking_system.unit.validation;

import com.ekgroup.booking_system.dto.CreateBookingRequest;
import com.ekgroup.booking_system.exception.ValidationException;
import com.ekgroup.booking_system.validation.BookingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingValidatorTest {

    private BookingValidator bookingValidator;
    private Clock clock;
    private LocalDate today;

    private static final List<LocalTime> ALLOWED_TIME_SLOTS = List.of(
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            LocalTime.of(14, 0),
            LocalTime.of(16, 0),
            LocalTime.of(18, 0)
    );

    @BeforeEach
    void setUp() {
        // Use a fixed clock to avoid issues with different timezones or time of day
        clock = Clock.system(ZoneId.of("Europe/Copenhagen"));
        today = LocalDate.now(clock);
        bookingValidator = new BookingValidator(ALLOWED_TIME_SLOTS, clock);
    }

    @Test
    void TC_BKG_001_validBookingDateAndAllowedTimeSlot_doesNotThrow() {
        CreateBookingRequest request = validRequest(
                today,
                LocalTime.of(10, 0)
        );

        assertDoesNotThrow(() -> bookingValidator.validateRequest(request));
    }

    @Test
    void TC_BKG_004_bookingDateYesterday_throwsValidationException() {
        CreateBookingRequest request = validRequest(
                today.minusDays(1),
                LocalTime.of(10, 0)
        );

        assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateRequest(request)
        );
    }

    @Test
    void TC_BKG_004_bookingDateToday_doesNotThrow() {
        CreateBookingRequest request = validRequest(
                today,
                LocalTime.of(10, 0)
        );

        assertDoesNotThrow(() -> bookingValidator.validateRequest(request));
    }

    @Test
    void TC_BKG_004_bookingDateTomorrow_doesNotThrow() {
        CreateBookingRequest request = validRequest(
                today.plusDays(1),
                LocalTime.of(10, 0)
        );

        assertDoesNotThrow(() -> bookingValidator.validateRequest(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"10:00", "12:00", "14:00", "16:00", "18:00"})
    void TC_BKG_001_allowedTimeSlots_doNotThrow(String time) {
        CreateBookingRequest request = validRequest(
                today.plusDays(1),
                LocalTime.parse(time)
        );

        assertDoesNotThrow(() -> bookingValidator.validateRequest(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"09:00", "11:30", "15:00", "19:00", "23:59"})
    void TC_BKG_005_invalidTimeSlot_throwsValidationException(String time) {
        CreateBookingRequest request = validRequest(
                today.plusDays(1),
                LocalTime.parse(time)
        );

        assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateRequest(request)
        );
    }

    private CreateBookingRequest validRequest(LocalDate bookingDate, LocalTime bookingTime) {
        return new CreateBookingRequest(
                1L,
                "Harry Potter",
                "harry@hogwarts.edu",
                bookingDate,
                bookingTime
        );
    }
}
