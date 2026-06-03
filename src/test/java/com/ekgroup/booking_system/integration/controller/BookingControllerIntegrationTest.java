package com.ekgroup.booking_system.integration.controller;

import com.ekgroup.booking_system.controller.BookingController;
import com.ekgroup.booking_system.dto.BookingResponse;
import com.ekgroup.booking_system.exception.GlobalExceptionHandler;
import com.ekgroup.booking_system.exception.NotFoundException;
import com.ekgroup.booking_system.model.BookingStatus;
import com.ekgroup.booking_system.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void TC_INT_CTRL_001_createBooking_validRequest_returnsCreatedBookingJson() throws Exception {
        BookingResponse response = new BookingResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                1L,
                "Quidditch Training",
                "Harry Potter",
                "harry@hogwarts.edu",
                LocalDate.of(2026, 5, 7),
                LocalTime.of(10, 30),
                BookingStatus.ACTIVE,
                LocalDateTime.of(2026, 5, 6, 12, 0),
                null
        );

        Mockito.when(bookingService.createBooking(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": 1,
                                  "customerName": "Harry Potter",
                                  "customerEmail": "harry@hogwarts.edu",
                                  "bookingDate": "2026-05-07",
                                  "bookingTime": "10:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("11111111-1111-1111-1111-111111111111")))
                .andExpect(jsonPath("$.activityId", is(1)))
                .andExpect(jsonPath("$.activityName", is("Quidditch Training")))
                .andExpect(jsonPath("$.customerName", is("Harry Potter")))
                .andExpect(jsonPath("$.customerEmail", is("harry@hogwarts.edu")))
                .andExpect(jsonPath("$.bookingDate", is("2026-05-07")))
                .andExpect(jsonPath("$.bookingTime", is("10:00:00")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void TC_INT_CTRL_002_getBooking_unknownId_returnsNotFoundProblemDetail() throws Exception {
        UUID bookingId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        Mockito.when(bookingService.getBooking(eq(bookingId)))
                .thenThrow(new NotFoundException("Booking not found."));

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource not found")))
                .andExpect(jsonPath("$.detail", is("Booking not found.")));
    }

    @Test
    void TC_INT_CTRL_003_createBooking_invalidPayload_returnsBadRequestProblemDetail() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": 1,
                                  "customerName": "",
                                  "customerEmail": "not-an-email",
                                  "bookingDate": "2026-05-07",
                                  "bookingTime": "10:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Invalid request payload")))
                .andExpect(jsonPath("$.detail", is("One or more fields are invalid. Check request body.")));
    }

    @Test
    void TC_INT_CTRL_004_cancelBooking_returnsCancelledResponse() throws Exception {
        UUID bookingId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        BookingResponse cancelled = new BookingResponse(
                bookingId,
                1L,
                "Quidditch Training",
                "Harry Potter",
                "harry@hogwarts.edu",
                LocalDate.of(2026, 5, 7),
                LocalTime.of(10, 0),
                BookingStatus.CANCELLED,
                LocalDateTime.of(2026, 5, 6, 12, 0),
                LocalDateTime.of(2026, 5, 6, 13, 0)
        );

        Mockito.when(bookingService.cancelBooking(eq(bookingId)))
                .thenReturn(cancelled);

        mockMvc.perform(delete("/api/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("33333333-3333-3333-3333-333333333333")))
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.cancelledAt", is("2026-05-06T13:00:00")));
    }
}
