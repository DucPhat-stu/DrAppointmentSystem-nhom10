package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.ScheduleRequest;
import com.healthcare.doctor.entity.DoctorScheduleEntity;
import com.healthcare.doctor.repository.DoctorScheduleJpaRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private DoctorScheduleJpaRepository scheduleRepository;

    private DoctorScheduleService service;

    @BeforeEach
    void setUp() {
        service = new DoctorScheduleService(scheduleRepository, FIXED_CLOCK);
    }

    @Test
    void createPersistsScheduleForCurrentDoctor() {
        UUID doctorId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 25);
        when(scheduleRepository.existsByDoctorIdAndDate(doctorId, date)).thenReturn(false);
        when(scheduleRepository.save(any(DoctorScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(doctorId, new ScheduleRequest(date));

        assertThat(response.id()).isNotNull();
        assertThat(response.date()).isEqualTo(date);
        verify(scheduleRepository).save(any(DoctorScheduleEntity.class));
    }

    @Test
    void createRejectsDuplicateDateForDoctor() {
        UUID doctorId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 25);
        when(scheduleRepository.existsByDoctorIdAndDate(doctorId, date)).thenReturn(true);

        assertThatThrownBy(() -> service.create(doctorId, new ScheduleRequest(date)))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void updateRejectsScheduleOwnedByAnotherDoctor() {
        UUID doctorId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        when(scheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(
                doctorId,
                scheduleId,
                new ScheduleRequest(LocalDate.of(2026, 6, 25))))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
