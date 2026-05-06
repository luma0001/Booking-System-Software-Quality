package com.EKGroup.booking_system.client.weather;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.EKGroup.booking_system.config.WeatherApiProperties;
import com.EKGroup.booking_system.dto.WeatherApiResponse;
import com.EKGroup.booking_system.exception.WeatherUnavailableException;
import com.EKGroup.booking_system.model.WeatherSnapshot;

@Component
public class WeatherClientImpl implements WeatherClient {
    private final WeatherApiProperties weatherApiProperties;
    private final RestClient restClient;

    public WeatherClientImpl(WeatherApiProperties weatherApiProperties, RestClient.Builder builder) { 
        this.weatherApiProperties = weatherApiProperties;
        this.restClient = builder.baseUrl(resolveBaseUrl(weatherApiProperties.baseUrl())).build();
    }

    @Override
    public WeatherSnapshot getWeather(LocalDate date, LocalTime time) {
        String apiKey = weatherApiProperties.key();
        String location = weatherApiProperties.location();

        var response = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/current.json")
            .queryParam("key", apiKey)
            .queryParam("q", location)
            .build())
        .retrieve()
        .body(WeatherApiResponse.class);

        if(response == null) { 
            throw new WeatherUnavailableException("Empty response from weather API");
        }

        return toWeatherSnapshot(response);
    }

    private WeatherSnapshot toWeatherSnapshot(WeatherApiResponse response) { 
        if (response.current() == null) {
            throw new WeatherUnavailableException("Weather API response did not include current weather.");
        }

        if (response.current().windKph() == null || response.current().tempC() == null || response.current().cloud() == null) {
            throw new WeatherUnavailableException("Weather API response is missing required weather measurements.");
        }

        double windMs = response.current().windKph() * 1000 / 60;
        double temperatureC = response.current().tempC();
        int cloudCoveragePercent = (int) Math.round(response.current().cloud());

        Long epoch = response.current().lastUpdatedEpoch();
        if (epoch == null && response.location() != null) {
            epoch = response.location().localTimeEpoch();
        }
        if (epoch == null) {
            throw new WeatherUnavailableException("Weather API response is missing timestamp information.");
        }

        Instant timeStamp = Instant.ofEpochSecond(epoch);
       
        return new WeatherSnapshot(
            windMs, 
            temperatureC, 
            cloudCoveragePercent, 
            timeStamp
        );
    }

    private String resolveBaseUrl(String configuredBaseUrl) {
        if (configuredBaseUrl == null || configuredBaseUrl.isBlank()) {
            throw new WeatherUnavailableException("Missing weather API base URL configuration.");
        }

        String normalizedBaseUrl = configuredBaseUrl.trim();
        URI uri = URI.create(normalizedBaseUrl);
        if (uri.getScheme() == null || uri.getScheme().isBlank()) {
            normalizedBaseUrl = "https://" + normalizedBaseUrl;
            uri = URI.create(normalizedBaseUrl);
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new WeatherUnavailableException("Invalid weather API base URL: " + configuredBaseUrl);
        }

        return normalizedBaseUrl;
    }

}
