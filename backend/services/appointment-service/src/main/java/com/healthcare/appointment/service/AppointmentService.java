package com.healthcare.appointment.service;

import com.healthcare.appointment.client.DoctorSlotClient;
import com.healthcare.appointment.domain.AppointmentStatus;
import com.healthcare.appointment.dto.AppointmentActionRequest;
import com.healthcare.appointment.dto.AppointmentOwnershipResponse;
import com.healthcare.appointment.dto.AppointmentPageResponse;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.entity.AppointmentActionIdempotencyEntity;
import com.healthcare.appointment.entity.AppointmentEntity;
import com.healthcare.appointment.events.AppointmentEventPublisher;
import com.healthcare.appointment.repository.AppointmentActionIdempotencyJpaRepository;
import com.healthcare.appointment.repository.AppointmentJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentJpaRepository appointmentRepository;
    private final AppointmentActionIdempotencyJpaRepository idempotencyRepository;
    private final AppointmentEventPublisher eventPublisher;
    private final DoctorSlotClient doctorSlotClient;

    public AppointmentService(AppointmentJpaRepository appointmentRepository,
                              AppointmentActionIdempotencyJpaRepository idempotencyRepository,
                              AppointmentEventPublisher eventPublisher,
                              DoctorSlotClient doctorSlotClient) {
        this.appointmentRepository = appointmentRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.eventPublisher = eventPublisher;
        this.doctorSlotClient = doctorSlotClient;
    }

    @Transactional(readOnly = true)
    public AppointmentPageResponse findDoctorAppointments(UUID doctorId,
                                                          LocalDate date,
                                                          AppointmentStatus status,
                                                          int page,
                                                          int size) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.ASC, "scheduledStart")
        );
        var appointments = appointmentRepository.findAll(filter(doctorId, date, status), pageRequest);
        return new AppointmentPageResponse(
                appointments.getContent().stream().map(this::toResponse).toList(),
                appointments.getNumber(),
                appointments.getSize(),
                appointments.getTotalElements(),
                appointments.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AppointmentPageResponse findDoctorPatientAppointments(UUID doctorId,
                                                                 UUID patientId,
                                                                 int page,
                                                                 int size) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "scheduledStart")
        );
        var appointments = appointmentRepository.findAll(filter(doctorId, patientId), pageRequest);
        return new AppointmentPageResponse(
                appointments.getContent().stream().map(this::toResponse).toList(),
                appointments.getNumber(),
                appointments.getSize(),
                appointments.getTotalElements(),
                appointments.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getDoctorAppointment(UUID doctorId, UUID appointmentId) {
        return toResponse(findOwned(doctorId, appointmentId));
    }

    @Transactional(readOnly = true)
    public AppointmentOwnershipResponse getDoctorAppointmentOwnership(UUID doctorId, UUID appointmentId) {
        AppointmentEntity appointment = findOwned(doctorId, appointmentId);
        return new AppointmentOwnershipResponse(
                appointment.getId(),
                appointment.getDoctorId(),
                appointment.getPatientId(),
                appointment.getStatus()
        );
    }

    @Transactional
    public AppointmentResponse confirm(UUID doctorId, UUID appointmentId, String idempotencyKey) {
        return transition(doctorId, appointmentId, "CONFIRM", idempotencyKey, AppointmentStatus.CONFIRMED, "APPOINTMENT_CONFIRMED", null);
    }

    @Transactional
    public AppointmentResponse reject(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request) {
        return transition(doctorId, appointmentId, "REJECT", idempotencyKey, AppointmentStatus.REJECTED, "APPOINTMENT_REJECTED", request.reason());
    }

    @Transactional
    public AppointmentResponse cancel(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request) {
        return transition(doctorId, appointmentId, "CANCEL", idempotencyKey, AppointmentStatus.CANCELLED, "APPOINTMENT_CANCELLED", request.reason());
    }

    private AppointmentResponse transition(UUID doctorId,
                                           UUID appointmentId,
                                           String action,
                                           String idempotencyKey,
                                           AppointmentStatus targetStatus,
                                           String eventName,
                                           String reason) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotencyRepository.findByAppointmentIdAndActionNameAndIdempotencyKey(
                    appointmentId,
                    action,
                    idempotencyKey
            );
            if (existing.isPresent()) {
                return toResponse(findOwned(doctorId, appointmentId));
            }
        }

        AppointmentEntity appointment = findOwned(doctorId, appointmentId);
        if (appointment.getStatus() == targetStatus) {
            recordIdempotency(appointment, action, idempotencyKey, targetStatus);
            return toResponse(appointment);
        }

        validateTransition(appointment.getStatus(), targetStatus);
        appointment.setStatus(targetStatus);
        if (targetStatus == AppointmentStatus.CANCELLED || targetStatus == AppointmentStatus.REJECTED) {
            appointment.setCancellationReason(reason);
        }

        AppointmentEntity saved = appointmentRepository.save(appointment);
        syncSlotStatus(saved, targetStatus);
        recordIdempotency(saved, action, idempotencyKey, targetStatus);
        eventPublisher.publishStatusChanged(eventName, saved);
        return toResponse(saved);
    }

    private void syncSlotStatus(AppointmentEntity appointment, AppointmentStatus targetStatus) {
        if (appointment.getSlotId() == null) {
            return;
        }
        if (targetStatus == AppointmentStatus.CONFIRMED) {
            doctorSlotClient.updateSlotStatus(appointment.getSlotId(), "BOOKED");
        } else if (targetStatus == AppointmentStatus.REJECTED || targetStatus == AppointmentStatus.CANCELLED) {
            doctorSlotClient.updateSlotStatus(appointment.getSlotId(), "AVAILABLE");
        }
    }

    private void validateTransition(AppointmentStatus currentStatus, AppointmentStatus targetStatus) {
        boolean valid = switch (targetStatus) {
            case CONFIRMED -> currentStatus == AppointmentStatus.PENDING;
            case REJECTED -> currentStatus == AppointmentStatus.PENDING;
            case CANCELLED -> currentStatus == AppointmentStatus.CONFIRMED || currentStatus == AppointmentStatus.PENDING;
            default -> false;
        };
        if (!valid) {
            throw new ApiException(ErrorCode.CONFLICT, "Invalid appointment state transition");
        }
    }

    private void recordIdempotency(AppointmentEntity appointment,
                                   String action,
                                   String idempotencyKey,
                                   AppointmentStatus targetStatus) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        try {
            idempotencyRepository.save(AppointmentActionIdempotencyEntity.create(
                    appointment.getId(),
                    appointment.getDoctorId(),
                    action,
                    idempotencyKey,
                    targetStatus
            ));
        } catch (DataIntegrityViolationException ignored) {
            // Duplicate idempotency keys are safe because state transitions are idempotent above.
        }
    }

    private AppointmentEntity findOwned(UUID doctorId, UUID appointmentId) {
        return appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
    }

    private Specification<AppointmentEntity> filter(UUID doctorId, LocalDate date, AppointmentStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("doctorId"), doctorId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (date != null) {
                OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
                OffsetDateTime end = start.plusDays(1);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("scheduledStart"), start));
                predicates.add(criteriaBuilder.lessThan(root.get("scheduledStart"), end));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<AppointmentEntity> filter(UUID doctorId, UUID patientId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("doctorId"), doctorId),
                criteriaBuilder.equal(root.get("patientId"), patientId)
        );
    }

    private AppointmentResponse toResponse(AppointmentEntity entity) {
        return new AppointmentResponse(
                entity.getId(),
                entity.getDoctorId(),
                entity.getPatientId(),
                entity.getSlotId(),
                entity.getScheduledStart(),
                entity.getScheduledEnd(),
                entity.getStatus(),
                entity.getReason(),
                entity.getCancellationReason()
        );
    }
}
