package com.EKGroup.booking_system.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.EKGroup.booking_system.client.weather.WeatherClient;
import com.EKGroup.booking_system.dto.WeatherAvailabilityResponse;
import com.EKGroup.booking_system.exception.NotFoundException;
import com.EKGroup.booking_system.model.Activity;
import com.EKGroup.booking_system.model.WeatherRuleType;
import com.EKGroup.booking_system.model.WeatherSnapshot;
import com.EKGroup.booking_system.repository.ActivityRepository;

@Service
public class WeatherService {

    private final ActivityRepository activityRepository;
    private final WeatherClient weatherClient;

    public WeatherService(ActivityRepository activityRepository, WeatherClient weatherClient) {
        this.activityRepository = activityRepository;
        this.weatherClient = weatherClient;
    }

    public WeatherAvailabilityResponse checkAvailability(long activityId, LocalDate date, LocalTime time) {
        Activity activity = activityRepository.findById(activityId)
                .filter(Activity::isActive)
                .orElseThrow(() -> new NotFoundException("Activity not found: " + activityId));
        WeatherSnapshot weather = weatherClient.getWeather(activity, date, time);
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
