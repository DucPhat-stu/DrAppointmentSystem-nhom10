package com.healthcare.appointment.service;

import com.healthcare.appointment.client.DoctorSlotClient;
import com.healthcare.appointment.entity.AppointmentSlotSyncJobEntity;
import com.healthcare.appointment.repository.AppointmentSlotSyncJobJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotSyncServiceTest {
    @Mock
    private AppointmentSlotSyncJobJpaRepository slotSyncJobRepository;

    @Mock
    private DoctorSlotClient doctorSlotClient;

    private SlotSyncService service;

    @BeforeEach
    void setUp() {
        service = new SlotSyncService(slotSyncJobRepository, doctorSlotClient);
    }

    @Test
    void processPendingJobsMarksJobProcessedWhenDoctorSlotUpdateSucceeds() {
        AppointmentSlotSyncJobEntity job = AppointmentSlotSyncJobEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESERVE",
                "BOOKED"
        );
        when(slotSyncJobRepository
                .findTop50ByProcessedAtIsNullAndAttemptsLessThanAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(eq(10), any(OffsetDateTime.class)))
                .thenReturn(List.of(job));

        service.processPendingJobs();

        verify(doctorSlotClient).updateSlotStatus(job.getSlotId(), "BOOKED");
        verify(slotSyncJobRepository).save(job);
        assertThat(job.getProcessedAt()).isNotNull();
        assertThat(job.getLastError()).isNull();
    }

    @Test
    void processPendingJobsKeepsJobPendingWhenDoctorSlotUpdateFails() {
        AppointmentSlotSyncJobEntity job = AppointmentSlotSyncJobEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RELEASE",
                "AVAILABLE"
        );
        when(slotSyncJobRepository
                .findTop50ByProcessedAtIsNullAndAttemptsLessThanAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(eq(10), any(OffsetDateTime.class)))
                .thenReturn(List.of(job));
        doThrow(new RuntimeException("doctor unavailable")).when(doctorSlotClient).updateSlotStatus(job.getSlotId(), "AVAILABLE");

        service.processPendingJobs();

        verify(slotSyncJobRepository).save(job);
        assertThat(job.getProcessedAt()).isNull();
        assertThat(job.getAttempts()).isEqualTo(1);
        assertThat(job.getLastError()).contains("doctor unavailable");
    }
}
