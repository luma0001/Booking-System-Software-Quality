package com.EKGroup.booking_system.repository;

import java.time.LocalTime;

public interface SlotCountProjection { 
    LocalTime getBookingTime(); 
    long getTotal();
}
