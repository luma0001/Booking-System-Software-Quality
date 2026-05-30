package com.ekgroup.booking_system.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ekgroup.booking_system.dto.BookingResponse;
import com.ekgroup.booking_system.dto.CreateBookingRequest;
import com.ekgroup.booking_system.exception.NotFoundException;
import com.ekgroup.booking_system.exception.ValidationException;
import com.ekgroup.booking_system.model.Activity;
import com.ekgroup.booking_system.model.Booking;
import com.ekgroup.booking_system.model.BookingStatus;
import com.ekgroup.booking_system.repository.ActivityRepository;
import com.ekgroup.booking_system.repository.BookingRepository;
import com.ekgroup.booking_system.validation.BookingValidator;

@Service
public class BookingService {

    private final ActivityRepository activityRepository;
    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;
    private final WeatherService weatherService;
    private final Clock clock;

    public BookingService(
            ActivityRepository activityRepository,
            BookingRepository bookingRepository,
            BookingValidator bookingValidator,
            WeatherService weatherService,
            Clock clock
    ) {
        this.activityRepository = activityRepository;
        this.bookingRepository = bookingRepository;
        this.bookingValidator = bookingValidator;
        this.weatherService = weatherService;
        this.clock = clock;
    }

    public List<BookingResponse> getBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        bookingValidator.validateRequest(request);

        Activity activity = activityRepository.findById(request.activityId())
                .filter(Activity::isActive)
                .orElseThrow(() -> new NotFoundException("Activity not found: " + request.activityId()));

        if (bookingRepository.existsByCustomerEmailAndActivityAndBookingDateAndBookingTimeAndStatus(
                request.customerEmail(),
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        )) {
            throw new ValidationException("This email already has an active booking for this slot.");
        }

        long participants = bookingRepository.countByActivityAndBookingDateAndBookingTimeAndStatus(
                activity,
                request.bookingDate(),
                request.bookingTime(),
                BookingStatus.ACTIVE
        );
        if (participants >= activity.getMaxParticipants()) {
            throw new ValidationException("Selected time slot is full.");
        }

        boolean weatherAllowed = weatherService.checkAvailability(
                request.activityId(),
                request.bookingDate(),
                request.bookingTime()
        ).allowed();
        if (!weatherAllowed) {
            throw new ValidationException("Booking rejected due to unsuitable weather.");
        }

        Booking booking = new Booking();
        booking.setActivity(activity);
        booking.setCustomerName(request.customerName());
        booking.setCustomerEmail(request.customerEmail());
        booking.setBookingDate(request.bookingDate());
        booking.setBookingTime(request.bookingTime());
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setCreatedAt(LocalDateTime.now(clock));

        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ValidationException("Booking is already cancelled.");
        }

        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getBookingTime());
        if (!bookingStart.isAfter(LocalDateTime.now(clock))) {
            throw new ValidationException("Booking can only be cancelled before it starts.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now(clock));
        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getActivity().getId(),
                booking.getActivity().getName(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getCancelledAt()
        );
    }
}
