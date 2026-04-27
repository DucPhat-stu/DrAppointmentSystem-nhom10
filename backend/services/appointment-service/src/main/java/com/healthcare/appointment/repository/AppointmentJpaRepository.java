package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.AppointmentEntity;
import com.healthcare.appointment.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID>, JpaSpecificationExecutor<AppointmentEntity> {
    Optional<AppointmentEntity> findByIdAndDoctorId(UUID id, UUID doctorId);

    Optional<AppointmentEntity> findByIdAndPatientId(UUID id, UUID patientId);

    boolean existsBySlotIdAndStatusIn(UUID slotId, Collection<AppointmentStatus> statuses);
}
