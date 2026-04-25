package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.ScheduleRequest;
import com.healthcare.doctor.dto.ScheduleResponse;
import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.entity.DoctorScheduleEntity;
import com.healthcare.doctor.repository.DoctorScheduleJpaRepository;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DoctorScheduleService {
    private final DoctorScheduleJpaRepository scheduleRepository;
    private final TimeSlotJpaRepository timeSlotRepository;
    private final Clock clock;

    public DoctorScheduleService(DoctorScheduleJpaRepository scheduleRepository,
                                 TimeSlotJpaRepository timeSlotRepository,
                                 Clock clock) {
        this.scheduleRepository = scheduleRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.clock = clock;
    }

    @Transactional
    public ScheduleResponse create(UUID doctorId, ScheduleRequest request) {
        LocalDate date = request.date();
        if (scheduleRepository.existsByDoctorIdAndDate(doctorId, date)) {
            throw new ApiException(ErrorCode.CONFLICT, "Doctor schedule already exists for date: " + date);
        }

        try {
            DoctorScheduleEntity saved = scheduleRepository.save(DoctorScheduleEntity.create(doctorId, date, clock));
            return toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT, "Doctor schedule already exists for date: " + date);
        }
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> list(UUID doctorId) {
        return scheduleRepository.findAllByDoctorIdOrderByDateAsc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ScheduleResponse update(UUID doctorId, UUID scheduleId, ScheduleRequest request) {
        DoctorScheduleEntity schedule = findOwnedSchedule(doctorId, scheduleId);
        LocalDate newDate = request.date();

        if (!schedule.getDate().equals(newDate)
                && scheduleRepository.existsByDoctorIdAndDateAndIdNot(doctorId, newDate, scheduleId)) {
            throw new ApiException(ErrorCode.CONFLICT, "Doctor schedule already exists for date: " + newDate);
        }

        schedule.setDate(newDate);
        try {
            return toResponse(scheduleRepository.save(schedule));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT, "Doctor schedule already exists for date: " + newDate);
        }
    }

    @Transactional
    public void delete(UUID doctorId, UUID scheduleId) {
        DoctorScheduleEntity schedule = findOwnedSchedule(doctorId, scheduleId);
        if (timeSlotRepository.existsByScheduleIdAndStatus(schedule.getId(), TimeSlotStatus.BOOKED)) {
            throw new ApiException(ErrorCode.CONFLICT, "Cannot delete a schedule with booked time slots");
        }
        scheduleRepository.delete(schedule);
    }

    private DoctorScheduleEntity findOwnedSchedule(UUID doctorId, UUID scheduleId) {
        return scheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor schedule not found"));
    }

    private ScheduleResponse toResponse(DoctorScheduleEntity entity) {
        return new ScheduleResponse(entity.getId(), entity.getDate());
    }
}
