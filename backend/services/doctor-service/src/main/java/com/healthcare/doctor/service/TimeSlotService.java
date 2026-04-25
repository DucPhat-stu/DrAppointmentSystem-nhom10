package com.healthcare.doctor.service;

import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.dto.TimeSlotRequest;
import com.healthcare.doctor.dto.TimeSlotResponse;
import com.healthcare.doctor.entity.DoctorScheduleEntity;
import com.healthcare.doctor.entity.TimeSlotEntity;
import com.healthcare.doctor.repository.DoctorScheduleJpaRepository;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class TimeSlotService {
    private final TimeSlotJpaRepository timeSlotRepository;
    private final DoctorScheduleJpaRepository scheduleRepository;
    private final Clock clock;

    public TimeSlotService(TimeSlotJpaRepository timeSlotRepository,
                           DoctorScheduleJpaRepository scheduleRepository,
                           Clock clock) {
        this.timeSlotRepository = timeSlotRepository;
        this.scheduleRepository = scheduleRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> list(UUID doctorId, UUID scheduleId) {
        DoctorScheduleEntity schedule = findOwnedSchedule(doctorId, scheduleId);
        return timeSlotRepository.findAllByScheduleIdOrderByStartTimeAsc(schedule.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TimeSlotResponse create(UUID doctorId, TimeSlotRequest request) {
        DoctorScheduleEntity schedule = findOwnedSchedule(doctorId, request.scheduleId());
        OffsetDateTime startTime = toUtc(request.startTime());
        OffsetDateTime endTime = toUtc(request.endTime());
        validateRange(schedule, startTime, endTime);
        if (timeSlotRepository.existsOverlap(schedule.getId(), startTime, endTime)) {
            throw new ApiException(ErrorCode.CONFLICT, "Time slot overlaps with an existing slot");
        }

        try {
            TimeSlotEntity saved = timeSlotRepository.save(TimeSlotEntity.create(
                    schedule.getId(),
                    startTime,
                    endTime,
                    clock
            ));
            return toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT, "Time slot overlaps with an existing slot");
        }
    }

    @Transactional
    public TimeSlotResponse update(UUID doctorId, UUID slotId, TimeSlotRequest request) {
        TimeSlotEntity slot = findSlot(slotId);
        DoctorScheduleEntity schedule = findOwnedSchedule(doctorId, slot.getScheduleId());
        if (!slot.getScheduleId().equals(request.scheduleId())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Time slot schedule cannot be changed");
        }
        if (slot.getStatus() == TimeSlotStatus.BOOKED) {
            throw new ApiException(ErrorCode.CONFLICT, "Booked time slots cannot be updated");
        }

        OffsetDateTime startTime = toUtc(request.startTime());
        OffsetDateTime endTime = toUtc(request.endTime());
        validateRange(schedule, startTime, endTime);
        if (timeSlotRepository.existsOverlapExcluding(schedule.getId(), slotId, startTime, endTime)) {
            throw new ApiException(ErrorCode.CONFLICT, "Time slot overlaps with an existing slot");
        }

        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        try {
            return toResponse(timeSlotRepository.save(slot));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT, "Time slot overlaps with an existing slot");
        }
    }

    @Transactional
    public void delete(UUID doctorId, UUID slotId) {
        TimeSlotEntity slot = findSlot(slotId);
        DoctorScheduleEntity schedule = findOwnedSchedule(doctorId, slot.getScheduleId());
        if (!schedule.getId().equals(slot.getScheduleId())) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Time slot not found");
        }
        if (slot.getStatus() == TimeSlotStatus.BOOKED) {
            throw new ApiException(ErrorCode.CONFLICT, "Booked time slots cannot be deleted");
        }
        timeSlotRepository.delete(slot);
    }

    @Transactional
    public TimeSlotResponse updateStatus(UUID slotId, TimeSlotStatus status) {
        TimeSlotEntity slot = findSlot(slotId);
        slot.setStatus(status);
        return toResponse(timeSlotRepository.save(slot));
    }

    private DoctorScheduleEntity findOwnedSchedule(UUID doctorId, UUID scheduleId) {
        return scheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor schedule not found"));
    }

    private TimeSlotEntity findSlot(UUID slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Time slot not found"));
    }

    private void validateRange(DoctorScheduleEntity schedule, OffsetDateTime startTime, OffsetDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Start time must be before end time");
        }
        if (!startTime.toLocalDate().equals(schedule.getDate())
                || !endTime.toLocalDate().equals(schedule.getDate())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Time slot must be within the schedule date");
        }
    }

    private OffsetDateTime toUtc(OffsetDateTime value) {
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }

    private TimeSlotResponse toResponse(TimeSlotEntity entity) {
        return new TimeSlotResponse(
                entity.getId(),
                entity.getScheduleId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus()
        );
    }
}
