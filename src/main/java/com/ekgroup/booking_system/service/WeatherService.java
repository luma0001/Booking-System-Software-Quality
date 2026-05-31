package com.ekgroup.booking_system.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ekgroup.booking_system.client.weather.WeatherClient;
import com.ekgroup.booking_system.dto.WeatherAvailabilityResponse;
import com.ekgroup.booking_system.exception.NotFoundException;
import com.ekgroup.booking_system.model.Activity;
import com.ekgroup.booking_system.model.WeatherRuleType;
import com.ekgroup.booking_system.model.WeatherSnapshot;
import com.ekgroup.booking_system.repository.ActivityRepository;

@Service
public class WeatherService {

    private final ActivityRepository activityRepository;
    private final WeatherClient weatherClient;

    @Value("${weather.mock.enabled:false}")
    private boolean mockWeather;

    public WeatherService(ActivityRepository activityRepository, WeatherClient weatherClient) {
        this.activityRepository = activityRepository;
        this.weatherClient = weatherClient;
    }

    public WeatherAvailabilityResponse checkAvailability(long activityId, LocalDate date, LocalTime time) {
        if (mockWeather) {
            return new WeatherAvailabilityResponse(
                    activityId,
                    date,
                    time,
                    true,
                    "Mock weather is suitable"
            );
        }
        Activity activity = activityRepository.findById(activityId)
                .filter(Activity::isActive)
                .orElseThrow(() -> new NotFoundException("Activity not found: " + activityId));
        WeatherSnapshot weather = weatherClient.getWeather(date, time);
        boolean allowed = isAllowed(activity.getWeatherRuleType(), activity.getWeatherThreshold(), weather);
        String reason = allowed
                ? "Weather conditions satisfy activity rule."
                : "Weather conditions violate activity rule.";

        return new WeatherAvailabilityResponse(activityId, date, time, allowed, reason);
    }

    public boolean isAllowed(WeatherRuleType weatherRuleType, double weatherThreshold, WeatherSnapshot weatherSnapshot) {
        if (weatherRuleType == WeatherRuleType.MAX_WIND_SPEED) {
            return weatherSnapshot.windSpeedMs() <= weatherThreshold;
        }
        if (weatherRuleType == WeatherRuleType.MIN_TEMPERATURE) {
            return weatherSnapshot.temperatureC() >= weatherThreshold;
        }
        if (weatherRuleType == WeatherRuleType.MAX_CLOUD_COVERAGE) {
            return weatherSnapshot.cloudCoveragePercent() <= weatherThreshold;
        }
        return false;
    }
}
