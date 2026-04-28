package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.AppointmentChangeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppointmentChangeHistoryJpaRepository extends JpaRepository<AppointmentChangeHistoryEntity, UUID> {
}
