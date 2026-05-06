package com.EKGroup.booking_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherApiResponse (
    CurrentWeather current,
    Location location
) {
    public record CurrentWeather(
        @JsonProperty("temp_c")
        Double tempC,

        @JsonProperty("wind_kph")
        Double windKph,

        @JsonProperty("cloud")
        Double cloud,

        @JsonProperty("last_updated_epoch")
        Long lastUpdatedEpoch
    ) {}

    public record Location(
        @JsonProperty("localtime_epoch")
        Long localTimeEpoch
    ) {}
}
