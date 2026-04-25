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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private TimeSlotJpaRepository timeSlotRepository;

    @Mock
    private DoctorScheduleJpaRepository scheduleRepository;

    private TimeSlotService service;

    @BeforeEach
    void setUp() {
        service = new TimeSlotService(timeSlotRepository, scheduleRepository, FIXED_CLOCK);
    }

    @Test
    void createPersistsAvailableSlotForOwnedSchedule() {
        UUID doctorId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        DoctorScheduleEntity schedule = schedule(scheduleId, doctorId, LocalDate.of(2026, 6, 25));
        OffsetDateTime start = OffsetDateTime.parse("2026-06-25T08:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-25T09:00:00Z");
        when(scheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)).thenReturn(Optional.of(schedule));
        when(timeSlotRepository.existsOverlap(scheduleId, start, end)).thenReturn(false);
        when(timeSlotRepository.save(any(TimeSlotEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeSlotResponse response = service.create(doctorId, new TimeSlotRequest(scheduleId, start, end));

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(TimeSlotStatus.AVAILABLE);
        assertThat(response.startTime()).isEqualTo(start);
    }

    @Test
    void createRejectsOverlap() {
        UUID doctorId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        DoctorScheduleEntity schedule = schedule(scheduleId, doctorId, LocalDate.of(2026, 6, 25));
        OffsetDateTime start = OffsetDateTime.parse("2026-06-25T08:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-25T09:00:00Z");
        when(scheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)).thenReturn(Optional.of(schedule));
        when(timeSlotRepository.existsOverlap(scheduleId, start, end)).thenReturn(true);

        assertThatThrownBy(() -> service.create(doctorId, new TimeSlotRequest(scheduleId, start, end)))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void deleteRejectsBookedSlot() {
        UUID doctorId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        DoctorScheduleEntity schedule = schedule(scheduleId, doctorId, LocalDate.of(2026, 6, 25));
        TimeSlotEntity slot = TimeSlotEntity.create(
                scheduleId,
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                OffsetDateTime.parse("2026-06-25T09:00:00Z"),
                FIXED_CLOCK
        );
        slot.setId(slotId);
        slot.setStatus(TimeSlotStatus.BOOKED);
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(scheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> service.delete(doctorId, slotId))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    private DoctorScheduleEntity schedule(UUID scheduleId, UUID doctorId, LocalDate date) {
        DoctorScheduleEntity schedule = DoctorScheduleEntity.create(doctorId, date, FIXED_CLOCK);
        schedule.setId(scheduleId);
        return schedule;
    }
}
