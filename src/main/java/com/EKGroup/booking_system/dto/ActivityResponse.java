package com.EKGroup.booking_system.dto;

public record ActivityResponse(
        Long id,
        String name,
        String description,
        String location,
        Integer maxParticipants
) {
}
