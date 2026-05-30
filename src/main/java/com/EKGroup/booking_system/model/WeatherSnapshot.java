package com.ekgroup.booking_system.model;

import java.time.Instant;

public record WeatherSnapshot(
        double windSpeedMs,
        double temperatureC,
        int cloudCoveragePercent,
        Instant timestamp
) {
}
