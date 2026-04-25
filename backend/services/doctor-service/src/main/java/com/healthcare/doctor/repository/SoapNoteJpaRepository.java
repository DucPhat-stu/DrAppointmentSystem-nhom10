package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.SoapNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SoapNoteJpaRepository extends JpaRepository<SoapNoteEntity, UUID> {
    Optional<SoapNoteEntity> findByAppointmentIdAndDoctorId(UUID appointmentId, UUID doctorId);
}
