package com.healthcare.doctor.service;

import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.dto.AvailableSlotResponse;
import com.healthcare.doctor.entity.TimeSlotEntity;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailableSlotServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private TimeSlotJpaRepository timeSlotRepository;

    @Mock
    private AvailableSlotCache availableSlotCache;

    private AvailableSlotService service;

    @BeforeEach
    void setUp() {
        service = new AvailableSlotService(timeSlotRepository, availableSlotCache);
    }

    @Test
    void findReturnsCachedSlotsWhenPresent() {
        UUID doctorId = UUID.randomUUID();
        LocalDate date = LocalDate.parse("2026-06-25");
        AvailableSlotResponse cached = new AvailableSlotResponse(
                UUID.randomUUID(),
                doctorId,
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                OffsetDateTime.parse("2026-06-25T09:00:00Z"),
                "AVAILABLE"
        );
        when(availableSlotCache.get(doctorId, date)).thenReturn(Optional.of(List.of(cached)));

        List<AvailableSlotResponse> response = service.find(doctorId, date);

        assertThat(response).containsExactly(cached);
        verify(timeSlotRepository, never()).findAllByDoctorIdAndDateAndStatus(doctorId, date, TimeSlotStatus.AVAILABLE);
    }

    @Test
    void findLoadsAvailableSlotsAndWritesCacheOnMiss() {
        UUID doctorId = UUID.randomUUID();
        LocalDate date = LocalDate.parse("2026-06-25");
        TimeSlotEntity slot = TimeSlotEntity.create(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                OffsetDateTime.parse("2026-06-25T09:00:00Z"),
                CLOCK
        );
        when(availableSlotCache.get(doctorId, date)).thenReturn(Optional.empty());
        when(timeSlotRepository.findAllByDoctorIdAndDateAndStatus(doctorId, date, TimeSlotStatus.AVAILABLE))
                .thenReturn(List.of(slot));

        List<AvailableSlotResponse> response = service.find(doctorId, date);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().status()).isEqualTo("AVAILABLE");
        verify(availableSlotCache).put(doctorId, date, response);
    }
}
