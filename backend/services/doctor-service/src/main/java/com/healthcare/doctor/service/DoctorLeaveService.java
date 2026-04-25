package com.healthcare.doctor.service;

import com.healthcare.doctor.domain.LeaveStatus;
import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.dto.DoctorLeavePageResponse;
import com.healthcare.doctor.dto.DoctorLeaveRequest;
import com.healthcare.doctor.dto.DoctorLeaveResponse;
import com.healthcare.doctor.dto.LeaveDecisionRequest;
import com.healthcare.doctor.entity.DoctorLeaveEntity;
import com.healthcare.doctor.repository.DoctorLeaveJpaRepository;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DoctorLeaveService {
    private static final List<LeaveStatus> ACTIVE_STATUSES = List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED);

    private final DoctorLeaveJpaRepository leaveRepository;
    private final TimeSlotJpaRepository timeSlotRepository;
    private final Clock clock;

    public DoctorLeaveService(DoctorLeaveJpaRepository leaveRepository,
                              TimeSlotJpaRepository timeSlotRepository,
                              Clock clock) {
        this.leaveRepository = leaveRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.clock = clock;
    }

    @Transactional
    public DoctorLeaveResponse create(UUID doctorId, DoctorLeaveRequest request) {
        validateRange(request.startDate(), request.endDate());
        if (leaveRepository.existsActiveOverlap(doctorId, request.startDate(), request.endDate(), ACTIVE_STATUSES)) {
            throw new ApiException(ErrorCode.CONFLICT, "Leave request overlaps with an existing active leave");
        }

        try {
            DoctorLeaveEntity saved = leaveRepository.save(DoctorLeaveEntity.create(
                    doctorId,
                    request.startDate(),
                    request.endDate(),
                    clock
            ));
            return toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT, "Leave request overlaps with an existing active leave");
        }
    }

    @Transactional(readOnly = true)
    public List<DoctorLeaveResponse> listDoctorLeaves(UUID doctorId) {
        return leaveRepository.findAllByDoctorIdOrderByStartDateDesc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorLeavePageResponse listAdminLeaves(LeaveStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        var leaves = leaveRepository.findAll(DoctorLeaveJpaRepository.statusIs(status), pageRequest);
        return new DoctorLeavePageResponse(
                leaves.getContent().stream().map(this::toResponse).toList(),
                leaves.getNumber(),
                leaves.getSize(),
                leaves.getTotalElements(),
                leaves.getTotalPages()
        );
    }

    @Transactional
    public DoctorLeaveResponse approve(UUID adminId, UUID leaveId) {
        DoctorLeaveEntity leave = findLeave(leaveId);
        if (leave.getStatus() == LeaveStatus.APPROVED) {
            return toResponse(leave);
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ApiException(ErrorCode.CONFLICT, "Only pending leave requests can be approved");
        }
        if (timeSlotRepository.existsByDoctorDateRangeAndStatus(
                leave.getDoctorId(),
                leave.getStartDate(),
                leave.getEndDate(),
                TimeSlotStatus.BOOKED)) {
            throw new ApiException(ErrorCode.CONFLICT, "Cannot approve leave with booked slots in the requested range");
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setDecidedBy(adminId);
        leave.setDecidedAt(OffsetDateTime.now(clock));
        leave.setRejectionReason(null);
        DoctorLeaveEntity saved = leaveRepository.save(leave);
        timeSlotRepository.blockAvailableSlotsForDoctorDateRange(
                saved.getDoctorId(),
                saved.getStartDate(),
                saved.getEndDate(),
                TimeSlotStatus.AVAILABLE,
                TimeSlotStatus.BLOCKED
        );
        return toResponse(saved);
    }

    @Transactional
    public DoctorLeaveResponse reject(UUID adminId, UUID leaveId, LeaveDecisionRequest request) {
        DoctorLeaveEntity leave = findLeave(leaveId);
        if (leave.getStatus() == LeaveStatus.REJECTED) {
            return toResponse(leave);
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ApiException(ErrorCode.CONFLICT, "Only pending leave requests can be rejected");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setDecidedBy(adminId);
        leave.setDecidedAt(OffsetDateTime.now(clock));
        leave.setRejectionReason(request != null ? normalizeReason(request.reason()) : null);
        return toResponse(leaveRepository.save(leave));
    }

    private DoctorLeaveEntity findLeave(UUID leaveId) {
        return leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Leave request not found"));
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Start date must be before end date");
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
    }

    private DoctorLeaveResponse toResponse(DoctorLeaveEntity entity) {
        return new DoctorLeaveResponse(
                entity.getId(),
                entity.getDoctorId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus(),
                entity.getRejectionReason(),
                entity.getDecidedBy(),
                entity.getDecidedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
