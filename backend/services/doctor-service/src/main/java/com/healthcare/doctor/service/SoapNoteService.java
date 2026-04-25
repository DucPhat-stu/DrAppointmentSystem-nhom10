package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.DoctorAppointmentOwnershipResponse;
import com.healthcare.doctor.dto.SoapNoteRequest;
import com.healthcare.doctor.dto.SoapNoteResponse;
import com.healthcare.doctor.entity.SoapNoteAuditLogEntity;
import com.healthcare.doctor.entity.SoapNoteEntity;
import com.healthcare.doctor.repository.SoapNoteAuditLogJpaRepository;
import com.healthcare.doctor.repository.SoapNoteJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SoapNoteService {
    private final SoapNoteJpaRepository soapNoteRepository;
    private final SoapNoteAuditLogJpaRepository auditLogRepository;
    private final DoctorAppointmentService appointmentService;
    private final SoapInputSanitizer sanitizer;

    public SoapNoteService(SoapNoteJpaRepository soapNoteRepository,
                           SoapNoteAuditLogJpaRepository auditLogRepository,
                           DoctorAppointmentService appointmentService,
                           SoapInputSanitizer sanitizer) {
        this.soapNoteRepository = soapNoteRepository;
        this.auditLogRepository = auditLogRepository;
        this.appointmentService = appointmentService;
        this.sanitizer = sanitizer;
    }

    @Transactional
    public SoapNoteResponse save(UUID doctorId, UUID appointmentId, SoapNoteRequest request) {
        DoctorAppointmentOwnershipResponse ownership = appointmentService.ownership(doctorId, appointmentId);
        SoapNoteEntity note = soapNoteRepository.findByAppointmentIdAndDoctorId(appointmentId, doctorId)
                .orElseGet(() -> createNew(ownership));
        String action = note.getId() == null ? "CREATE" : "UPDATE";

        note.setSubjective(sanitizer.sanitize(request.subjective()));
        note.setObjective(sanitizer.sanitize(request.objective()));
        note.setAssessment(sanitizer.sanitize(request.assessment()));
        note.setPlan(sanitizer.sanitize(request.plan()));

        SoapNoteEntity saved = soapNoteRepository.save(note);
        auditLogRepository.save(SoapNoteAuditLogEntity.create(saved.getId(), appointmentId, doctorId, action));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SoapNoteResponse get(UUID doctorId, UUID appointmentId) {
        appointmentService.ownership(doctorId, appointmentId);
        return soapNoteRepository.findByAppointmentIdAndDoctorId(appointmentId, doctorId)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "SOAP note not found"));
    }

    private SoapNoteEntity createNew(DoctorAppointmentOwnershipResponse ownership) {
        SoapNoteEntity note = new SoapNoteEntity();
        note.setAppointmentId(ownership.appointmentId());
        note.setDoctorId(ownership.doctorId());
        note.setPatientId(ownership.patientId());
        return note;
    }

    private SoapNoteResponse toResponse(SoapNoteEntity entity) {
        return new SoapNoteResponse(
                entity.getId(),
                entity.getAppointmentId(),
                entity.getDoctorId(),
                entity.getPatientId(),
                entity.getSubjective(),
                entity.getObjective(),
                entity.getAssessment(),
                entity.getPlan(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
