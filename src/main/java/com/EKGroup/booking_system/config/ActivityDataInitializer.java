package com.ekgroup.booking_system.config;

import com.ekgroup.booking_system.model.Activity;
import com.ekgroup.booking_system.model.WeatherRuleType;
import com.ekgroup.booking_system.repository.ActivityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ActivityDataInitializer {

    @Bean
    CommandLineRunner seedActivities(ActivityRepository activityRepository) {
        return args -> {
            if (activityRepository.count() > 0) {
                return;
            }

            List<Activity> activities = List.of(
                    new Activity(
                            "Quidditch Training",
                            "Flying drills and coordinated team practice on brooms.",
                            "Quidditch Pitch",
                            10,
                            WeatherRuleType.MAX_WIND_SPEED,
                            12.0,
                            true
                    ),
                    new Activity(
                            "Herbology Field Lesson",
                            "Outdoor practical session identifying magical plants.",
                            "Greenhouse Area",
                            12,
                            WeatherRuleType.MIN_TEMPERATURE,
                            0.0,
                            true
                    ),
                    new Activity(
                            "Astronomy Tower Session",
                            "Observation session using telescope charts and star maps.",
                            "Astronomy Tower",
                            8,
                            WeatherRuleType.MAX_CLOUD_COVERAGE,
                            80.0,
                            true
                    ),
                    new Activity(
                            "Lake Observation",
                            "Guided observation and notes near the Black Lake.",
                            "Black Lake",
                            6,
                            WeatherRuleType.MAX_WIND_SPEED,
                            10.0,
                            true
                    )
            );

            activityRepository.saveAll(activities);
        };
    }
}
