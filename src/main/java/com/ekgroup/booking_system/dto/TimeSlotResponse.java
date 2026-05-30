package com.ekgroup.booking_system.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TimeSlotResponse(
        Long activityId,
        LocalDate date,
        List<LocalTime> availableSlots
) {
}
