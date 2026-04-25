package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.DoctorScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorScheduleJpaRepository extends JpaRepository<DoctorScheduleEntity, UUID> {
    boolean existsByDoctorIdAndDate(UUID doctorId, LocalDate date);

    boolean existsByDoctorIdAndDateAndIdNot(UUID doctorId, LocalDate date, UUID id);

    List<DoctorScheduleEntity> findAllByDoctorIdOrderByDateAsc(UUID doctorId);

    Optional<DoctorScheduleEntity> findByIdAndDoctorId(UUID id, UUID doctorId);
}
