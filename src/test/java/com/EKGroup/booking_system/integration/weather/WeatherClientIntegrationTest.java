package com.EKGroup.booking_system.integration.weather;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.EKGroup.booking_system.client.weather.WeatherClientImpl;
import com.EKGroup.booking_system.config.WeatherApiProperties;
import com.EKGroup.booking_system.model.WeatherSnapshot;

class WeatherClientIntegrationTest {

    private MockRestServiceServer mockServer;
    private WeatherClientImpl weatherClient;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        var properties = new WeatherApiProperties("test-api-key", "https://weather.example", "London");
        weatherClient = new WeatherClientImpl(properties, builder);
    }

    @Test
    void getWeather_mapsApiResponseToWeatherSnapshot() {
        mockServer.expect(requestTo("https://weather.example/current.json?key=test-api-key&q=London"))
                .andRespond(withSuccess("""
                    {
                      "location": {
                        "localtime_epoch": 1710000000
                      },
                      "current": {
                        "temp_c": 21.2,
                        "wind_kph": 18.0,
                        "cloud": 33.3,
                        "last_updated_epoch": 1710000000
                      }
                    }
                    """, MediaType.APPLICATION_JSON));

        WeatherSnapshot result = weatherClient.getWeather(LocalDate.now(), LocalTime.NOON);

        assertEquals(21.2, result.temperatureC());
        assertEquals(300.0, result.windSpeedMs());
        assertEquals(33, result.cloudCoveragePercent());
        assertEquals(Instant.ofEpochSecond(1710000000), result.timestamp());
        mockServer.verify();
    }

    @Test
    void getWeather_throwsForNon2xxResponse() {
        mockServer.expect(requestTo("https://weather.example/current.json?key=test-api-key&q=London"))
                .andRespond(withServerError());

        RestClientResponseException exception = assertThrows(RestClientResponseException.class,
                () -> weatherClient.getWeather(LocalDate.now(), LocalTime.NOON));
        assertEquals(500, exception.getStatusCode().value());
        mockServer.verify();
    }
}
