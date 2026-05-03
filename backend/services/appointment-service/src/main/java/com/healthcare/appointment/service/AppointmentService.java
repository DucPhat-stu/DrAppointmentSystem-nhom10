package com.healthcare.appointment.service;

import com.healthcare.appointment.client.DoctorSlotClient;
import com.healthcare.appointment.domain.AppointmentStatus;
import com.healthcare.appointment.dto.AppointmentActionRequest;
import com.healthcare.appointment.dto.AppointmentOwnershipResponse;
import com.healthcare.appointment.dto.AppointmentPageResponse;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.dto.CreateAppointmentRequest;
import com.healthcare.appointment.dto.DoctorSlotResponse;
import com.healthcare.appointment.dto.RescheduleAppointmentRequest;
import com.healthcare.appointment.entity.AppointmentActionIdempotencyEntity;
import com.healthcare.appointment.entity.AppointmentChangeHistoryEntity;
import com.healthcare.appointment.entity.AppointmentEntity;
import com.healthcare.appointment.events.AppointmentEventPublisher;
import com.healthcare.appointment.repository.AppointmentActionIdempotencyJpaRepository;
import com.healthcare.appointment.repository.AppointmentChangeHistoryJpaRepository;
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
    private static final List<AppointmentStatus> ACTIVE_SLOT_STATUSES = List.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED
    );

    private final AppointmentJpaRepository appointmentRepository;
    private final AppointmentActionIdempotencyJpaRepository idempotencyRepository;
    private final AppointmentChangeHistoryJpaRepository changeHistoryRepository;
    private final AppointmentEventPublisher eventPublisher;
    private final DoctorSlotClient doctorSlotClient;
    private final SlotSyncService slotSyncService;

    public AppointmentService(AppointmentJpaRepository appointmentRepository,
                              AppointmentActionIdempotencyJpaRepository idempotencyRepository,
                              AppointmentChangeHistoryJpaRepository changeHistoryRepository,
                              AppointmentEventPublisher eventPublisher,
                              DoctorSlotClient doctorSlotClient,
                              SlotSyncService slotSyncService) {
        this.appointmentRepository = appointmentRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.changeHistoryRepository = changeHistoryRepository;
        this.eventPublisher = eventPublisher;
        this.doctorSlotClient = doctorSlotClient;
        this.slotSyncService = slotSyncService;
    }

    @Transactional
    public AppointmentResponse create(UUID patientId, CreateAppointmentRequest request) {
        DoctorSlotResponse slot = doctorSlotClient.getSlot(request.slotId());
        if (!request.doctorId().equals(slot.doctorId())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Slot does not belong to the selected doctor");
        }
        if (!"AVAILABLE".equals(slot.status())) {
            throw new ApiException(ErrorCode.CONFLICT, "Selected slot is no longer available");
        }
        if (appointmentRepository.existsBySlotIdAndStatusIn(request.slotId(), ACTIVE_SLOT_STATUSES)) {
            throw new ApiException(ErrorCode.CONFLICT, "Selected slot is already booked");
        }

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setId(UUID.randomUUID());
        appointment.setDoctorId(request.doctorId());
        appointment.setPatientId(patientId);
        appointment.setSlotId(request.slotId());
        appointment.setScheduledStart(slot.startTime());
        appointment.setScheduledEnd(slot.endTime());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setReason(normalizeReason(request.reason()));

        try {
            AppointmentEntity saved = appointmentRepository.save(appointment);
            slotSyncService.enqueueReserve(saved);
            eventPublisher.publishStatusChanged("APPOINTMENT_REQUESTED", saved);
            return toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT, "Selected slot is already booked");
        }
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getPatientAppointment(UUID patientId, UUID appointmentId) {
        AppointmentEntity appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
        return toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentPageResponse findPatientAppointments(UUID patientId,
                                                           String rawStatus,
                                                           int page,
                                                           int size) {
        AppointmentStatus status = parseStatus(rawStatus);
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "scheduledStart")
        );
        var appointments = appointmentRepository.findAll(filterPatient(patientId, status), pageRequest);
        return new AppointmentPageResponse(
                appointments.getContent().stream().map(this::toResponse).toList(),
                appointments.getNumber(),
                appointments.getSize(),
                appointments.getTotalElements(),
                appointments.getTotalPages()
        );
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

    @Transactional
    public AppointmentResponse complete(UUID doctorId, UUID appointmentId, String idempotencyKey) {
        return transition(doctorId, appointmentId, "COMPLETE", idempotencyKey, AppointmentStatus.COMPLETED, "APPOINTMENT_COMPLETED", null);
    }

    // -------------------------------------------------------------------------
    // Admin methods
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AppointmentPageResponse findAllAppointments(String rawStatus, int page, int size) {
        AppointmentStatus status = parseStatus(rawStatus);
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "scheduledStart")
        );
        var appointments = appointmentRepository.findAll(filterByStatus(status), pageRequest);
        return new AppointmentPageResponse(
                appointments.getContent().stream().map(this::toResponse).toList(),
                appointments.getNumber(),
                appointments.getSize(),
                appointments.getTotalElements(),
                appointments.getTotalPages()
        );
    }

    @Transactional
    public AppointmentResponse adminCancelAppointment(UUID adminId,
                                                      UUID appointmentId,
                                                      String reason) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return toResponse(appointment);
        }

        validateTransition(appointment.getStatus(), AppointmentStatus.CANCELLED);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason != null && !reason.isBlank() ? reason.trim() : "Cancelled by admin");

        AppointmentEntity saved = appointmentRepository.save(appointment);
        if (saved.getSlotId() != null) {
            slotSyncService.enqueueRelease(saved);
        }
        eventPublisher.publishStatusChanged("APPOINTMENT_CANCELLED_BY_ADMIN", saved);
        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancelPatientAppointment(UUID patientId,
                                                        UUID appointmentId,
                                                        String idempotencyKey,
                                                        AppointmentActionRequest request) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotencyRepository.findByAppointmentIdAndActionNameAndIdempotencyKey(
                    appointmentId,
                    "PATIENT_CANCEL",
                    idempotencyKey
            );
            if (existing.isPresent()) {
                return getPatientAppointment(patientId, appointmentId);
            }
        }

        AppointmentEntity appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            recordIdempotency(appointment, "PATIENT_CANCEL", idempotencyKey, AppointmentStatus.CANCELLED);
            return toResponse(appointment);
        }

        validateTransition(appointment.getStatus(), AppointmentStatus.CANCELLED);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(request.reason());

        AppointmentEntity saved = appointmentRepository.save(appointment);
        enqueueSlotSync(saved, AppointmentStatus.CANCELLED);
        recordIdempotency(saved, "PATIENT_CANCEL", idempotencyKey, AppointmentStatus.CANCELLED);
        eventPublisher.publishStatusChanged("APPOINTMENT_CANCELLED_BY_PATIENT", saved);
        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse reschedulePatientAppointment(UUID patientId,
                                                            UUID appointmentId,
                                                            String idempotencyKey,
                                                            RescheduleAppointmentRequest request) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotencyRepository.findByAppointmentIdAndActionNameAndIdempotencyKey(
                    appointmentId,
                    "PATIENT_RESCHEDULE",
                    idempotencyKey
            );
            if (existing.isPresent()) {
                return getPatientAppointment(patientId, appointmentId);
            }
        }

        AppointmentEntity appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
        validatePatientReschedule(appointment);
        if (request.slotId().equals(appointment.getSlotId())) {
            recordIdempotency(appointment, "PATIENT_RESCHEDULE", idempotencyKey, appointment.getStatus());
            return toResponse(appointment);
        }

        DoctorSlotResponse newSlot = doctorSlotClient.getSlot(request.slotId());
        if (!appointment.getDoctorId().equals(newSlot.doctorId())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "New slot must belong to the same doctor");
        }
        if (!"AVAILABLE".equals(newSlot.status())) {
            throw new ApiException(ErrorCode.CONFLICT, "Selected slot is no longer available");
        }
        if (appointmentRepository.existsBySlotIdAndStatusIn(request.slotId(), ACTIVE_SLOT_STATUSES)) {
            throw new ApiException(ErrorCode.CONFLICT, "Selected slot is already booked");
        }

        UUID oldSlotId = appointment.getSlotId();
        OffsetDateTime oldScheduledStart = appointment.getScheduledStart();
        OffsetDateTime oldScheduledEnd = appointment.getScheduledEnd();
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setSlotId(newSlot.id());
        appointment.setScheduledStart(newSlot.startTime());
        appointment.setScheduledEnd(newSlot.endTime());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setReason(normalizeReason(request.reason()));
        appointment.setCancellationReason(null);

        AppointmentEntity saved = appointmentRepository.save(appointment);
        slotSyncService.enqueueReserve(saved);
        if (oldSlotId != null) {
            slotSyncService.enqueueRelease(saved.getId(), oldSlotId);
        }
        changeHistoryRepository.save(AppointmentChangeHistoryEntity.reschedule(
                patientId,
                saved,
                oldSlotId,
                oldScheduledStart,
                oldScheduledEnd,
                oldStatus,
                normalizeReason(request.reason())
        ));
        recordIdempotency(saved, "PATIENT_RESCHEDULE", idempotencyKey, AppointmentStatus.PENDING);
        eventPublisher.publishStatusChanged("APPOINTMENT_RESCHEDULED", saved);
        return toResponse(saved);
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
        validateCompletionTime(appointment, targetStatus);
        appointment.setStatus(targetStatus);
        if (targetStatus == AppointmentStatus.CANCELLED || targetStatus == AppointmentStatus.REJECTED) {
            appointment.setCancellationReason(reason);
        }

        AppointmentEntity saved = appointmentRepository.save(appointment);
        enqueueSlotSync(saved, targetStatus);
        recordIdempotency(saved, action, idempotencyKey, targetStatus);
        eventPublisher.publishStatusChanged(eventName, saved);
        return toResponse(saved);
    }

    private void enqueueSlotSync(AppointmentEntity appointment, AppointmentStatus targetStatus) {
        if (appointment.getSlotId() == null) {
            return;
        }
        if (targetStatus == AppointmentStatus.CONFIRMED) {
            slotSyncService.enqueueReserve(appointment);
        } else if (targetStatus == AppointmentStatus.REJECTED || targetStatus == AppointmentStatus.CANCELLED) {
            slotSyncService.enqueueRelease(appointment);
        }
    }

    private void validateTransition(AppointmentStatus currentStatus, AppointmentStatus targetStatus) {
        boolean valid = switch (targetStatus) {
            case CONFIRMED -> currentStatus == AppointmentStatus.PENDING;
            case REJECTED -> currentStatus == AppointmentStatus.PENDING;
            case CANCELLED -> currentStatus == AppointmentStatus.CONFIRMED || currentStatus == AppointmentStatus.PENDING;
            case COMPLETED -> currentStatus == AppointmentStatus.CONFIRMED;
            default -> false;
        };
        if (!valid) {
            throw new ApiException(ErrorCode.CONFLICT, "Invalid appointment state transition");
        }
    }

    private void validatePatientReschedule(AppointmentEntity appointment) {
        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ApiException(ErrorCode.CONFLICT, "Only pending or confirmed appointments can be rescheduled");
        }
    }

    private void validateCompletionTime(AppointmentEntity appointment, AppointmentStatus targetStatus) {
        if (targetStatus == AppointmentStatus.COMPLETED && appointment.getScheduledEnd().isAfter(OffsetDateTime.now())) {
            throw new ApiException(ErrorCode.CONFLICT, "Future appointments cannot be completed");
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

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
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

    private Specification<AppointmentEntity> filterPatient(UUID patientId, AppointmentStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("patientId"), patientId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<AppointmentEntity> filterByStatus(AppointmentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    private AppointmentStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        try {
            return AppointmentStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Invalid appointment status");
        }
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
