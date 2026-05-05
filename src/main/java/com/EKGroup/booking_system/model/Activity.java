package com.EKGroup.booking_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WeatherRuleType weatherRuleType;

    @Column(nullable = false)
    private Double weatherThreshold;

    @Column(nullable = false)
    private boolean active = true;

    public Activity() {
    }

    public Activity(
            String name,
            String description,
            String location,
            Integer maxParticipants,
            WeatherRuleType weatherRuleType,
            Double weatherThreshold,
            boolean active
    ) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.weatherRuleType = weatherRuleType;
        this.weatherThreshold = weatherThreshold;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public WeatherRuleType getWeatherRuleType() {
        return weatherRuleType;
    }

    public Double getWeatherThreshold() {
        return weatherThreshold;
    }

    public boolean isActive() {
        return active;
    }
}
