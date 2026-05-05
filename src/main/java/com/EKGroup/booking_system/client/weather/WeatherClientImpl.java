package com.EKGroup.booking_system.client.weather;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.EKGroup.booking_system.config.WeatherApiProperties;
import com.EKGroup.booking_system.exception.WeatherUnavailableException;
import com.EKGroup.booking_system.model.Activity;
import com.EKGroup.booking_system.model.WeatherSnapshot;

@Component
public class WeatherClientImpl implements WeatherClient {
    private final WeatherApiProperties weatherApiProperties;

    public WeatherClientImpl(WeatherApiProperties weatherApiProperties) { 
        this.weatherApiProperties = weatherApiProperties;
    }

    @Override
    public WeatherSnapshot getWeather(Activity activity, LocalDate date, LocalTime time) {
        String apiKey = weatherApiProperties.key();
        throw new WeatherUnavailableException("Weather API integration is not configured yet.");
    }
}
