package com.healthcare.doctor.service;

import com.healthcare.doctor.domain.LeaveStatus;
import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.dto.DoctorLeaveRequest;
import com.healthcare.doctor.dto.LeaveDecisionRequest;
import com.healthcare.doctor.entity.DoctorLeaveEntity;
import com.healthcare.doctor.repository.DoctorLeaveJpaRepository;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorLeaveServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private DoctorLeaveJpaRepository leaveRepository;

    @Mock
    private TimeSlotJpaRepository timeSlotRepository;

    @Mock
    private AvailableSlotCache availableSlotCache;

    private DoctorLeaveService service;

    @BeforeEach
    void setUp() {
        service = new DoctorLeaveService(leaveRepository, timeSlotRepository, availableSlotCache, CLOCK);
    }

    @Test
    void createRejectsOverlappingActiveLeave() {
        UUID doctorId = UUID.randomUUID();
        DoctorLeaveRequest request = new DoctorLeaveRequest(LocalDate.parse("2026-06-10"), LocalDate.parse("2026-06-12"));
        when(leaveRepository.existsActiveOverlap(
                eq(doctorId),
                eq(request.startDate()),
                eq(request.endDate()),
                eq(List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED)))).thenReturn(true);

        assertThatThrownBy(() -> service.create(doctorId, request))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void approveBlocksAvailableSlotsWhenNoBookedSlotsExist() {
        UUID adminId = UUID.randomUUID();
        UUID leaveId = UUID.randomUUID();
        DoctorLeaveEntity leave = leave(leaveId, UUID.randomUUID(), LeaveStatus.PENDING);
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(leave));
        when(timeSlotRepository.existsByDoctorDateRangeAndStatus(
                leave.getDoctorId(),
                leave.getStartDate(),
                leave.getEndDate(),
                TimeSlotStatus.BOOKED)).thenReturn(false);
        when(leaveRepository.save(leave)).thenReturn(leave);

        var response = service.approve(adminId, leaveId);

        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(response.decidedBy()).isEqualTo(adminId);
        verify(timeSlotRepository).blockAvailableSlotsForDoctorDateRange(
                leave.getDoctorId(),
                leave.getStartDate(),
                leave.getEndDate(),
                TimeSlotStatus.AVAILABLE,
                TimeSlotStatus.BLOCKED
        );
        verify(availableSlotCache).evict(leave.getDoctorId(), LocalDate.parse("2026-06-10"));
        verify(availableSlotCache).evict(leave.getDoctorId(), LocalDate.parse("2026-06-11"));
    }

    @Test
    void approveRejectsWhenBookedSlotsExist() {
        UUID leaveId = UUID.randomUUID();
        DoctorLeaveEntity leave = leave(leaveId, UUID.randomUUID(), LeaveStatus.PENDING);
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(leave));
        when(timeSlotRepository.existsByDoctorDateRangeAndStatus(
                leave.getDoctorId(),
                leave.getStartDate(),
                leave.getEndDate(),
                TimeSlotStatus.BOOKED)).thenReturn(true);

        assertThatThrownBy(() -> service.approve(UUID.randomUUID(), leaveId))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(timeSlotRepository, never()).blockAvailableSlotsForDoctorDateRange(any(), any(), any(), any(), any());
    }

    @Test
    void rejectIsIdempotentForAlreadyRejectedLeave() {
        UUID leaveId = UUID.randomUUID();
        DoctorLeaveEntity leave = leave(leaveId, UUID.randomUUID(), LeaveStatus.REJECTED);
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(leave));

        var response = service.reject(UUID.randomUUID(), leaveId, new LeaveDecisionRequest("Duplicate"));

        assertThat(response.status()).isEqualTo(LeaveStatus.REJECTED);
        verify(leaveRepository, never()).save(any());
    }

    private DoctorLeaveEntity leave(UUID leaveId, UUID doctorId, LeaveStatus status) {
        DoctorLeaveEntity leave = DoctorLeaveEntity.create(
                doctorId,
                LocalDate.parse("2026-06-10"),
                LocalDate.parse("2026-06-12"),
                CLOCK
        );
        leave.setId(leaveId);
        leave.setStatus(status);
        return leave;
    }
}
