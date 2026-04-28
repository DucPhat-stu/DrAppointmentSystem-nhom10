package com.healthcare.appointment.service;

import com.healthcare.appointment.client.DoctorSlotClient;
import com.healthcare.appointment.entity.AppointmentEntity;
import com.healthcare.appointment.entity.AppointmentSlotSyncJobEntity;
import com.healthcare.appointment.repository.AppointmentSlotSyncJobJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SlotSyncService {
    private static final int MAX_ATTEMPTS = 10;

    private final AppointmentSlotSyncJobJpaRepository slotSyncJobRepository;
    private final DoctorSlotClient doctorSlotClient;

    public SlotSyncService(AppointmentSlotSyncJobJpaRepository slotSyncJobRepository,
                           DoctorSlotClient doctorSlotClient) {
        this.slotSyncJobRepository = slotSyncJobRepository;
        this.doctorSlotClient = doctorSlotClient;
    }

    public void enqueueReserve(AppointmentEntity appointment) {
        enqueue(appointment.getId(), appointment.getSlotId(), "RESERVE", "BOOKED");
    }

    public void enqueueRelease(AppointmentEntity appointment) {
        enqueue(appointment.getId(), appointment.getSlotId(), "RELEASE", "AVAILABLE");
    }

    public void enqueueRelease(UUID appointmentId, UUID slotId) {
        enqueue(appointmentId, slotId, "RELEASE", "AVAILABLE");
    }

    private void enqueue(UUID appointmentId, UUID slotId, String operation, String targetStatus) {
        if (slotId == null) {
            return;
        }
        slotSyncJobRepository.save(AppointmentSlotSyncJobEntity.create(appointmentId, slotId, operation, targetStatus));
    }

    @Scheduled(fixedDelayString = "${app.slot-sync.fixed-delay-ms:5000}")
    @Transactional
    public void processPendingJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        for (AppointmentSlotSyncJobEntity job : slotSyncJobRepository
                .findTop50ByProcessedAtIsNullAndAttemptsLessThanAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(MAX_ATTEMPTS, now)) {
            process(job, now);
        }
    }

    private void process(AppointmentSlotSyncJobEntity job, OffsetDateTime attemptedAt) {
        try {
            doctorSlotClient.updateSlotStatus(job.getSlotId(), job.getTargetStatus());
            job.markProcessed(attemptedAt);
        } catch (RuntimeException exception) {
            job.markFailed(exception.getMessage(), attemptedAt);
        }
        slotSyncJobRepository.save(job);
    }
}
