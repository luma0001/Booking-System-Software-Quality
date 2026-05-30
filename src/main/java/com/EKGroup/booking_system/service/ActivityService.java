package com.ekgroup.booking_system.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ekgroup.booking_system.dto.ActivityResponse;
import com.ekgroup.booking_system.dto.TimeSlotResponse;
import com.ekgroup.booking_system.exception.NotFoundException;
import com.ekgroup.booking_system.model.Activity;
import com.ekgroup.booking_system.model.BookingStatus;
import com.ekgroup.booking_system.repository.ActivityRepository;
import com.ekgroup.booking_system.repository.BookingRepository;
import com.ekgroup.booking_system.repository.SlotCountProjection;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final BookingRepository bookingRepository;
    private final List<LocalTime> allowedTimeSlots;

    public ActivityService(
            ActivityRepository activityRepository,
            BookingRepository bookingRepository,
            List<LocalTime> allowedTimeSlots
    ) {
        this.activityRepository = activityRepository;
        this.bookingRepository = bookingRepository;
        this.allowedTimeSlots = allowedTimeSlots;
    }

    public List<ActivityResponse> getActivities() {
        return activityRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(activity -> new ActivityResponse(
                        activity.getId(),
                        activity.getName(),
                        activity.getDescription(),
                        activity.getLocation(),
                        activity.getMaxParticipants()
                ))
                .toList();
    }

    public TimeSlotResponse getAvailableSlots(long activityId, LocalDate date) {
        Activity activity = activityRepository.findById(activityId)
                .filter(Activity::isActive)
                .orElseThrow(() -> new NotFoundException("Activity not found: " + activityId));

        List<SlotCountProjection> bookingTimeCountMap = bookingRepository.countActiveBookingsPerSlot(activity, date, BookingStatus.ACTIVE);

        Map<LocalTime, Long> bookedBySlot = bookingTimeCountMap.stream().collect(Collectors.toMap(SlotCountProjection::getBookingTime, SlotCountProjection::getTotal));

        List<LocalTime> availableSlots = allowedTimeSlots.stream()
        .filter(slot -> bookedBySlot.getOrDefault(slot, 0L) < activity.getMaxParticipants()).toList();

        return new TimeSlotResponse(activity.getId(), date, availableSlots);
    }
}
