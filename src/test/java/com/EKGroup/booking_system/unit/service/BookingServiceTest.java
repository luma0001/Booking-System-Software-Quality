package com.EKGroup.booking_system.unit.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.EKGroup.booking_system.dto.BookingResponse;
import com.EKGroup.booking_system.dto.CreateBookingRequest;
import com.EKGroup.booking_system.dto.WeatherAvailabilityResponse;
import com.EKGroup.booking_system.exception.NotFoundException;
import com.EKGroup.booking_system.exception.ValidationException;
import com.EKGroup.booking_system.model.Activity;
import com.EKGroup.booking_system.model.Booking;
import com.EKGroup.booking_system.model.BookingStatus;
import com.EKGroup.booking_system.model.WeatherRuleType;
import com.EKGroup.booking_system.repository.ActivityRepository;
import com.EKGroup.booking_system.repository.BookingRepository;
import com.EKGroup.booking_system.service.BookingService;
import com.EKGroup.booking_system.service.WeatherService;
import com.EKGroup.booking_system.validation.BookingValidator;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingValidator bookingValidator;

    @Mock
    private WeatherService weatherService;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                activityRepository,
                bookingRepository,
                bookingValidator,
                weatherService
        );
    }

    @Test
    void TC_BKG_001_validBooking_savesBookingAndReturnsResponse() {
        CreateBookingRequest request = validRequest();
        Activity activity = validActivity();

        when(activityRepository.findById(request.activityId()))
                .thenReturn(Optional.of(activity));

        when(bookingRepository.existsByCustomerEmailAndActivityAndBookingDateAndBookingTimeAndStatus(
                request.customerEmail(),
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(false);

        when(bookingRepository.countByActivityAndBookingDateAndBookingTimeAndStatus(
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(0L);

        when(weatherService.checkAvailability(
                request.activityId(),
                request.bookingDate(),
                request.bookingTime()
        )).thenReturn(weatherAllowed(request));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.createBooking(request);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        Booking savedBooking = bookingCaptor.getValue();

        assertEquals(activity, savedBooking.getActivity());
        assertEquals("Harry Potter", savedBooking.getCustomerName());
        assertEquals("harry@hogwarts.edu", savedBooking.getCustomerEmail());
        assertEquals(request.bookingDate(), savedBooking.getBookingDate());
        assertEquals(request.bookingTime(), savedBooking.getBookingTime());
        assertEquals(BookingStatus.ACTIVE, savedBooking.getStatus());
        assertNotNull(savedBooking.getCreatedAt());

        assertEquals("Harry Potter", response.customerName());
        assertEquals("harry@hogwarts.edu", response.customerEmail());
        assertEquals(request.bookingDate(), response.bookingDate());
        assertEquals(request.bookingTime(), response.bookingTime());
        assertEquals(BookingStatus.ACTIVE, response.status());

        verify(bookingValidator).validateRequest(request);
    }

    @Test
    void TC_BKG_006_nonExistingActivity_throwsNotFoundException() {
        CreateBookingRequest request = validRequest();

        when(activityRepository.findById(request.activityId()))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.createBooking(request)
        );

        verify(bookingValidator).validateRequest(request);
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(weatherService);
    }

    @Test
    void TC_BKG_006_inactiveActivity_throwsNotFoundException() {
        CreateBookingRequest request = validRequest();
        Activity inactiveActivity = inactiveActivity();

        when(activityRepository.findById(request.activityId()))
                .thenReturn(Optional.of(inactiveActivity));

        assertThrows(
                NotFoundException.class,
                () -> bookingService.createBooking(request)
        );

        verify(bookingValidator).validateRequest(request);
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(weatherService);
    }

    @Test
    void TC_BKG_008_duplicateBooking_throwsValidationException() {
        CreateBookingRequest request = validRequest();
        Activity activity = validActivity();

        when(activityRepository.findById(request.activityId()))
                .thenReturn(Optional.of(activity));

        when(bookingRepository.existsByCustomerEmailAndActivityAndBookingDateAndBookingTimeAndStatus(
                request.customerEmail(),
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(true);

        assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(request)
        );

        verify(bookingValidator).validateRequest(request);
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(weatherService);
    }

    @Test
    void TC_BKG_007_fullTimeSlot_throwsValidationException() {
        CreateBookingRequest request = validRequest();
        Activity activity = validActivity();

        when(activityRepository.findById(request.activityId()))
                .thenReturn(Optional.of(activity));

        when(bookingRepository.existsByCustomerEmailAndActivityAndBookingDateAndBookingTimeAndStatus(
                request.customerEmail(),
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(false);

        when(bookingRepository.countByActivityAndBookingDateAndBookingTimeAndStatus(
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(10L);

        assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(request)
        );

        verify(bookingValidator).validateRequest(request);
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(weatherService);
    }

    @Test
    void TC_WEA_002_unsuitableWeather_throwsValidationException() {
        CreateBookingRequest request = validRequest();
        Activity activity = validActivity();

        when(activityRepository.findById(request.activityId()))
                .thenReturn(Optional.of(activity));

        when(bookingRepository.existsByCustomerEmailAndActivityAndBookingDateAndBookingTimeAndStatus(
                request.customerEmail(),
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(false);

        when(bookingRepository.countByActivityAndBookingDateAndBookingTimeAndStatus(
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(0L);

        when(weatherService.checkAvailability(
                request.activityId(),
                request.bookingDate(),
                request.bookingTime()
        )).thenReturn(weatherRejected(request));

        assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(request)
        );

        verify(bookingValidator).validateRequest(request);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void TC_BKG_007_slotWithOneSeatLeft_savesBooking() {
        CreateBookingRequest request = validRequest();
        Activity activity = validActivity();

        when(activityRepository.findById(request.activityId()))
                .thenReturn(Optional.of(activity));

        when(bookingRepository.existsByCustomerEmailAndActivityAndBookingDateAndBookingTimeAndStatus(
                request.customerEmail(),
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(false);

        when(bookingRepository.countByActivityAndBookingDateAndBookingTimeAndStatus(
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )).thenReturn(9L);

        when(weatherService.checkAvailability(
                request.activityId(),
                request.bookingDate(),
                request.bookingTime()
        )).thenReturn(weatherAllowed(request));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> bookingService.createBooking(request));

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void getBooking_existingBooking_returnsBookingResponse() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = activeFutureBooking();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBooking(bookingId);

        assertEquals("Harry Potter", response.customerName());
        assertEquals("harry@hogwarts.edu", response.customerEmail());
        assertEquals(BookingStatus.ACTIVE, response.status());
    }

    @Test
    void getBooking_nonExistingBooking_throwsNotFoundException() {
        UUID bookingId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(bookingId)
        );
    }

    @Test
    void TC_BKG_008_cancelActiveFutureBooking_setsStatusToCancelled() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = activeFutureBooking();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, response.status());
        assertNotNull(response.cancelledAt());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        Booking savedBooking = bookingCaptor.getValue();
        assertEquals(BookingStatus.CANCELLED, savedBooking.getStatus());
        assertNotNull(savedBooking.getCancelledAt());
    }

    @Test
    void TC_BKG_008_cancelAlreadyCancelledBooking_throwsValidationException() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = activeFutureBooking();
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        assertThrows(
                ValidationException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelNonExistingBooking_throwsNotFoundException() {
        UUID bookingId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void TC_BKG_009_cancelBookingAfterStartTime_throwsValidationException() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = activeFutureBooking();
        booking.setBookingDate(LocalDate.now().minusDays(1));
        booking.setBookingTime(LocalTime.of(10, 0));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        assertThrows(
                ValidationException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void TC_BKG_010_cancelBookingExactlyAtStartTime_throwsValidationException() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = activeFutureBooking();
        LocalDateTime now = LocalDateTime.now();
        booking.setBookingDate(now.toLocalDate());
        booking.setBookingTime(now.toLocalTime());

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        assertThrows(
                ValidationException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        verify(bookingRepository, never()).save(any());
    }

    private CreateBookingRequest validRequest() {
        return new CreateBookingRequest(
                1L,
                "Harry Potter",
                "harry@hogwarts.edu",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0)
        );
    }

    private WeatherAvailabilityResponse weatherAllowed(CreateBookingRequest request) {
        return new WeatherAvailabilityResponse(
                request.activityId(),
                request.bookingDate(),
                request.bookingTime(),
                true,
                "Weather conditions satisfy activity rule."
        );
    }

    private WeatherAvailabilityResponse weatherRejected(CreateBookingRequest request) {
        return new WeatherAvailabilityResponse(
                request.activityId(),
                request.bookingDate(),
                request.bookingTime(),
                false,
                "Weather conditions violate activity rule."
        );
    }

    private Activity validActivity() {
        return new Activity(
                "Quidditch Training",
                "Outdoor Quidditch training session",
                "Quidditch Pitch",
                10,
                WeatherRuleType.MAX_WIND_SPEED,
                12.0,
                true
        );
    }

    private Activity inactiveActivity() {
        return new Activity(
                "Inactive Quidditch Training",
                "Inactive outdoor Quidditch training session",
                "Quidditch Pitch",
                10,
                WeatherRuleType.MAX_WIND_SPEED,
                12.0,
                false
        );
    }

    private Booking activeFutureBooking() {
        Booking booking = new Booking();
        booking.setActivity(validActivity());
        booking.setCustomerName("Harry Potter");
        booking.setCustomerEmail("harry@hogwarts.edu");
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setBookingTime(LocalTime.of(10, 0));
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setCreatedAt(LocalDateTime.now().minusHours(1));
        return booking;
    }
}