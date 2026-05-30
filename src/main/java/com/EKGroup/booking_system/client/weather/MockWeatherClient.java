package com.ekgroup.booking_system.client.weather;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ekgroup.booking_system.model.WeatherSnapshot;

@Component
@Profile("test")
public class MockWeatherClient implements WeatherClient {
    private static final Logger logger = LoggerFactory.getLogger(MockWeatherClient.class);

    @Override
    public WeatherSnapshot getWeather(LocalDate date, LocalTime time) {
        logger.info("Using MockWeatherClient to get weather for {} at {}", date, time);
        // Return a default "good" weather snapshot
        return new WeatherSnapshot(
            5.0,  // windSpeedMs
            20.0, // temperatureC
            20,   // cloudCoveragePercent
            Instant.now()
        );
    }
}
