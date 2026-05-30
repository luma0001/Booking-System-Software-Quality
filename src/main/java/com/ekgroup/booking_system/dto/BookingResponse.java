package com.ekgroup.booking_system.dto;

import com.ekgroup.booking_system.model.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        Long activityId,
        String activityName,
        String customerName,
        String customerEmail,
        LocalDate bookingDate,
        LocalTime bookingTime,
        BookingStatus status,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
}
