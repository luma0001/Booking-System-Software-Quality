package com.EKGroup.booking_system.unit.exception;

import com.EKGroup.booking_system.exception.GlobalExceptionHandler;
import com.EKGroup.booking_system.exception.NotFoundException;
import com.EKGroup.booking_system.exception.ValidationException;
import com.EKGroup.booking_system.exception.WeatherUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void TC_EX_001_notFoundException_returns404ProblemDetail() {
        ProblemDetail detail = handler.handleNotFound(new NotFoundException("Booking not found."));

        assertEquals(HttpStatus.NOT_FOUND.value(), detail.getStatus());
        assertEquals("Resource not found", detail.getTitle());
        assertEquals("Booking not found.", detail.getDetail());
    }

    @Test
    void TC_EX_002_validationException_returns400ProblemDetail() {
        ProblemDetail detail = handler.handleValidation(new ValidationException("Selected time slot is full."));

        assertEquals(HttpStatus.BAD_REQUEST.value(), detail.getStatus());
        assertEquals("Validation error", detail.getTitle());
        assertEquals("Selected time slot is full.", detail.getDetail());
    }

    @Test
    void TC_EX_003_weatherUnavailableException_returns503ProblemDetail() {
        ProblemDetail detail = handler.handleWeatherUnavailable(
                new WeatherUnavailableException("Empty response from weather API"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), detail.getStatus());
        assertEquals("Weather service unavailable", detail.getTitle());
        assertEquals("Empty response from weather API", detail.getDetail());
    }

    @Test
    void TC_EX_004_methodArgumentNotValid_returns400ProblemDetail() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        ProblemDetail detail = handler.handleBeanValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), detail.getStatus());
        assertEquals("Invalid request payload", detail.getTitle());
        assertEquals("One or more fields are invalid. Check request body.", detail.getDetail());
    }
}
