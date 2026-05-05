package com.EKGroup.booking_system.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.EKGroup.booking_system.dto.ActivityResponse;
import com.EKGroup.booking_system.dto.TimeSlotResponse;
import com.EKGroup.booking_system.service.ActivityService;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivityResponse> getActivities() {
        return activityService.getActivities();
    }

    @GetMapping("/{activityId}/slots")
    public TimeSlotResponse getAvailableSlots(
            @PathVariable long activityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return activityService.getAvailableSlots(activityId, date);
    }
}
