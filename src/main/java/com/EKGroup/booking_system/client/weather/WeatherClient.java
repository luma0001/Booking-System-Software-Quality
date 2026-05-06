package com.EKGroup.booking_system.client.weather;

import java.time.LocalDate;
import java.time.LocalTime;

import com.EKGroup.booking_system.model.WeatherSnapshot;

public interface WeatherClient {
    WeatherSnapshot getWeather(LocalDate date, LocalTime time);
}
