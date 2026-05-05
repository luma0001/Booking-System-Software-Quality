package com.EKGroup.booking_system.exception;

public class WeatherUnavailableException extends RuntimeException {
    public WeatherUnavailableException(String message) {
        super(message);
    }
}
