package com.EKGroup.booking_system.unit.client;

import com.EKGroup.booking_system.client.weather.WeatherClientImpl;
import com.EKGroup.booking_system.config.WeatherApiProperties;
import com.EKGroup.booking_system.exception.WeatherUnavailableException;
import com.EKGroup.booking_system.model.WeatherSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherClientImplTest {

    private static final String EXPECTED_URL =
            "https://weather.example/current.json?key=test-api-key&q=London";

    private RestClient.Builder builder;
    private MockRestServiceServer mockServer;
    private WeatherClientImpl weatherClient;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        weatherClient = new WeatherClientImpl(
                new WeatherApiProperties("test-api-key", "https://weather.example", "London"),
                builder
        );
    }

    @Test
    void TC_WC_010_toWeatherSnapshot_currentNull_throws() {
        mockServer.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        WeatherUnavailableException ex = assertThrows(WeatherUnavailableException.class,
                () -> weatherClient.getWeather(LocalDate.now(), LocalTime.NOON));
        assertEquals("Weather API response did not include current weather.", ex.getMessage());
        mockServer.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{ \"current\": { \"temp_c\": 20.0, \"cloud\": 30.0, \"last_updated_epoch\": 1710000000 } }",
            "{ \"current\": { \"wind_kph\": 18.0, \"cloud\": 30.0, \"last_updated_epoch\": 1710000000 } }",
            "{ \"current\": { \"wind_kph\": 18.0, \"temp_c\": 20.0, \"last_updated_epoch\": 1710000000 } }"
    })
    void TC_WC_011_toWeatherSnapshot_missingMeasurement_throws(String body) {
        mockServer.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        WeatherUnavailableException ex = assertThrows(WeatherUnavailableException.class,
                () -> weatherClient.getWeather(LocalDate.now(), LocalTime.NOON));
        assertEquals("Weather API response is missing required weather measurements.", ex.getMessage());
        mockServer.verify();
    }

    @Test
    void TC_WC_012_toWeatherSnapshot_epochFallback_usesLocationEpoch() {
        mockServer.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "temp_c": 21.2,
                            "wind_kph": 18.0,
                            "cloud": 33.3
                          },
                          "location": {
                            "localtime_epoch": 1710000123
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherSnapshot snapshot = weatherClient.getWeather(LocalDate.now(), LocalTime.NOON);

        assertEquals(Instant.ofEpochSecond(1710000123), snapshot.timestamp());
        mockServer.verify();
    }

    @Test
    void TC_WC_013_toWeatherSnapshot_bothEpochsNull_throws() {
        mockServer.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "temp_c": 21.2,
                            "wind_kph": 18.0,
                            "cloud": 33.3
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherUnavailableException ex = assertThrows(WeatherUnavailableException.class,
                () -> weatherClient.getWeather(LocalDate.now(), LocalTime.NOON));
        assertEquals("Weather API response is missing timestamp information.", ex.getMessage());
        mockServer.verify();
    }

    @Test
    void TC_WC_014_getWeather_emptyBody_throws() {
        mockServer.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess());

        WeatherUnavailableException ex = assertThrows(WeatherUnavailableException.class,
                () -> weatherClient.getWeather(LocalDate.now(), LocalTime.NOON));
        assertEquals("Empty response from weather API", ex.getMessage());
        mockServer.verify();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''",
            "'   '",
            "NULL"
    }, nullValues = "NULL")
    void TC_WC_020_resolveBaseUrl_blank_throws(String configuredBaseUrl) {
        WeatherApiProperties properties = new WeatherApiProperties("k", configuredBaseUrl, "London");
        RestClient.Builder b = RestClient.builder();

        WeatherUnavailableException ex = assertThrows(WeatherUnavailableException.class,
                () -> new WeatherClientImpl(properties, b));
        assertEquals("Missing weather API base URL configuration.", ex.getMessage());
    }

    @Test
    void TC_WC_021_resolveBaseUrl_missingScheme_prependsHttps() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();
        WeatherClientImpl client = new WeatherClientImpl(
                new WeatherApiProperties("test-api-key", "weather.example", "London"),
                b
        );

        server.expect(requestTo("https://weather.example/current.json?key=test-api-key&q=London"))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "temp_c": 21.2,
                            "wind_kph": 18.0,
                            "cloud": 33.3,
                            "last_updated_epoch": 1710000000
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherSnapshot snapshot = client.getWeather(LocalDate.now(), LocalTime.NOON);

        assertEquals(21.2, snapshot.temperatureC());
        server.verify();
    }

    @Test
    void TC_WC_022_resolveBaseUrl_invalidHost_throws() {
        WeatherApiProperties properties = new WeatherApiProperties("k", "https:///no-host", "London");
        RestClient.Builder b = RestClient.builder();

        assertThrows(WeatherUnavailableException.class,
                () -> new WeatherClientImpl(properties, b));
    }
}
