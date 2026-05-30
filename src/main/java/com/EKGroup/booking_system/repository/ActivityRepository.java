package com.ekgroup.booking_system.repository;

import com.ekgroup.booking_system.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByActiveTrueOrderByIdAsc();
}
