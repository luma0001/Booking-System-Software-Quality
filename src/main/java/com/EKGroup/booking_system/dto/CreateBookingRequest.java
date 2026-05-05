package com.EKGroup.booking_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateBookingRequest(
        @NotNull
        @Positive
        Long activityId,

        @NotBlank
        @Size(min = 2, max = 30)
        String customerName,

        @NotBlank
        @Email
        String customerEmail,

        @NotNull
        LocalDate bookingDate,

        @NotNull
        LocalTime bookingTime
) {
}
