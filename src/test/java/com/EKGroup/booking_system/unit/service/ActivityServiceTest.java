package com.EKGroup.booking_system.unit.service;

import com.EKGroup.booking_system.dto.ActivityResponse;
import com.EKGroup.booking_system.dto.TimeSlotResponse;
import com.EKGroup.booking_system.exception.NotFoundException;
import com.EKGroup.booking_system.model.Activity;
import com.EKGroup.booking_system.model.BookingStatus;
import com.EKGroup.booking_system.model.WeatherRuleType;
import com.EKGroup.booking_system.repository.ActivityRepository;
import com.EKGroup.booking_system.repository.BookingRepository;
import com.EKGroup.booking_system.repository.SlotCountProjection;
import com.EKGroup.booking_system.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    private static final long ACTIVITY_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 7);

    private static final List<LocalTime> ALLOWED_TIME_SLOTS = List.of(
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            LocalTime.of(14, 0),
            LocalTime.of(16, 0),
            LocalTime.of(18, 0)
    );

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private BookingRepository bookingRepository;

    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityService(activityRepository, bookingRepository, ALLOWED_TIME_SLOTS);
    }

    @Test
    void TC_ACT_001_getActivities_returnsMappedDtosInRepoOrder() {
        Activity quidditch = activity(1L, "Quidditch Training", "Outdoor session", "Quidditch Pitch", 10);
        Activity herbology = activity(2L, "Herbology Field Lesson", "Plants", "Greenhouse", 12);

        when(activityRepository.findByActiveTrueOrderByIdAsc())
                .thenReturn(List.of(quidditch, herbology));

        List<ActivityResponse> response = activityService.getActivities();

        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals("Quidditch Training", response.get(0).name());
        assertEquals("Outdoor session", response.get(0).description());
        assertEquals("Quidditch Pitch", response.get(0).location());
        assertEquals(10, response.get(0).maxParticipants());
        assertEquals(2L, response.get(1).id());
        assertEquals("Herbology Field Lesson", response.get(1).name());
    }

    @Test
    void TC_ACT_002_getActivities_emptyRepository_returnsEmptyList() {
        when(activityRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of());

        List<ActivityResponse> response = activityService.getActivities();

        assertTrue(response.isEmpty());
    }

    @Test
    void TC_ACT_010_getAvailableSlots_activityNotFound_throwsNotFoundException() {
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> activityService.getAvailableSlots(ACTIVITY_ID, DATE));
    }

    @Test
    void TC_ACT_011_getAvailableSlots_inactiveActivity_throwsNotFoundException() {
        Activity inactive = activity(ACTIVITY_ID, "Quidditch", "x", "x", 10, false);
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(inactive));

        assertThrows(NotFoundException.class,
                () -> activityService.getAvailableSlots(ACTIVITY_ID, DATE));
    }

    @Test
    void TC_ACT_012_getAvailableSlots_filtersOutFullSlots() {
        Activity activity = activity(ACTIVITY_ID, "Quidditch", "x", "x", 10, true);
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        when(bookingRepository.countActiveBookingsPerSlot(activity, DATE, BookingStatus.ACTIVE))
                .thenReturn(List.of(slotCount(LocalTime.of(12, 0), 10L)));

        TimeSlotResponse response = activityService.getAvailableSlots(ACTIVITY_ID, DATE);

        assertEquals(ACTIVITY_ID, response.activityId());
        assertEquals(DATE, response.date());
        assertEquals(4, response.availableSlots().size());
        assertFalse(response.availableSlots().contains(LocalTime.of(12, 0)));
    }

    @Test
    void TC_ACT_013_getAvailableSlots_noBookings_returnsAllSlots() {
        Activity activity = activity(ACTIVITY_ID, "Quidditch", "x", "x", 10, true);
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        when(bookingRepository.countActiveBookingsPerSlot(activity, DATE, BookingStatus.ACTIVE))
                .thenReturn(List.of());

        TimeSlotResponse response = activityService.getAvailableSlots(ACTIVITY_ID, DATE);

        assertEquals(ALLOWED_TIME_SLOTS, response.availableSlots());
    }

    @Test
    void TC_ACT_014_getAvailableSlots_partialBookingsKept_boundary() {
        Activity activity = activity(ACTIVITY_ID, "Quidditch", "x", "x", 10, true);
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        when(bookingRepository.countActiveBookingsPerSlot(activity, DATE, BookingStatus.ACTIVE))
                .thenReturn(List.of(slotCount(LocalTime.of(14, 0), 9L)));

        TimeSlotResponse response = activityService.getAvailableSlots(ACTIVITY_ID, DATE);

        assertEquals(5, response.availableSlots().size());
        assertTrue(response.availableSlots().contains(LocalTime.of(14, 0)));
    }

    private static Activity activity(Long id, String name, String description, String location, int maxParticipants) {
        return activity(id, name, description, location, maxParticipants, true);
    }

    private static Activity activity(Long id, String name, String description, String location, int maxParticipants, boolean active) {
        Activity activity = new Activity(
                name,
                description,
                location,
                maxParticipants,
                WeatherRuleType.MAX_WIND_SPEED,
                12.0,
                active
        );
        try {
            Field idField = Activity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(activity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set Activity id via reflection", e);
        }
        return activity;
    }

    private static SlotCountProjection slotCount(LocalTime bookingTime, long total) {
        return new SlotCountProjection() {
            @Override
            public LocalTime getBookingTime() {
                return bookingTime;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }
}
