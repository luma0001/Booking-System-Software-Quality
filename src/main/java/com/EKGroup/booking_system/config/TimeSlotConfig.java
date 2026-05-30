package com.ekgroup.booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Configuration
public class TimeSlotConfig {

    public static final ZoneId BOOKING_ZONE = ZoneId.of("Europe/Copenhagen");

    @Bean
    public Clock clock() {
        return Clock.system(BOOKING_ZONE);
    }

    @Bean
    public List<LocalTime> allowedTimeSlots() {
        return List.of(
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                LocalTime.of(18, 0)
        );
    }
}
