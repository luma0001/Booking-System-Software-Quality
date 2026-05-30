package com.ekgroup.booking_system.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record WeatherAvailabilityResponse(
        Long activityId,
        LocalDate date,
        LocalTime time,
        boolean allowed,
        String reason
) {
}
