package com.healthcare.user.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.user.dto.CreateMedicalRecordRequest;
import com.healthcare.user.dto.MedicalRecordResponse;
import com.healthcare.user.entity.MedicalRecordEntity;
import com.healthcare.user.entity.UserProfileEntity;
import com.healthcare.user.repository.MedicalRecordJpaRepository;
import com.healthcare.user.repository.UserProfileJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {
    private final MedicalRecordJpaRepository recordRepository;
    private final UserProfileJpaRepository profileRepository;
    private final ObjectMapper objectMapper;

    public MedicalRecordService(MedicalRecordJpaRepository recordRepository,
                                UserProfileJpaRepository profileRepository,
                                ObjectMapper objectMapper) {
        this.recordRepository = recordRepository;
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all medical records for a patient (by auth userId).
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getRecords(UUID userId) {
        UserProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Profile not found for user: " + userId));

        return recordRepository.findByPatientIdOrderByVisitDateDesc(profile.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getRecordsByPatientUserId(UUID patientUserId) {
        return getRecords(patientUserId);
    }

    /**
     * Get a single medical record detail (patient-scoped).
     */
    @Transactional(readOnly = true)
    public MedicalRecordResponse getRecordDetail(UUID userId, UUID recordId) {
        UserProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Profile not found for user: " + userId));

        MedicalRecordEntity record = recordRepository.findByIdAndPatientId(recordId, profile.getId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Medical record not found: " + recordId));

        return toResponse(record);
    }

    /**
     * Create a new medical record (doctor-only).
     */
    @Transactional
    public MedicalRecordResponse createRecord(UUID doctorUserId, String doctorName,
                                               CreateMedicalRecordRequest request) {
        UUID patientUserId = UUID.fromString(request.patientUserId());
        UserProfileEntity patientProfile = profileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Patient not found: " + request.patientUserId()));

        MedicalRecordEntity entity = new MedicalRecordEntity();
        entity.setId(UUID.randomUUID());
        entity.setRecordCode(generateRecordCode());
        entity.setPatientId(patientProfile.getId());
        entity.setDoctorName(doctorName);
        entity.setDepartment(request.department());
        entity.setDiseaseSummary(request.diseaseSummary());
        entity.setPrescription(request.prescription());
        entity.setVisitDate(request.visitDate());
        entity.setAppointmentDate(request.appointmentDate());
        entity.setCheckinTime(request.checkinTime());
        entity.setNotes(request.notes());
        entity.setCreatedAt(OffsetDateTime.now());

        // Serialize tests list to JSON string
        if (request.tests() != null && !request.tests().isEmpty()) {
            try {
                entity.setTests(objectMapper.writeValueAsString(request.tests()));
            } catch (Exception e) {
                entity.setTests("[]");
            }
        }

        recordRepository.save(entity);
        return toResponse(entity);
    }

    private MedicalRecordResponse toResponse(MedicalRecordEntity entity) {
        List<String> tests = parseTests(entity.getTests());
        return new MedicalRecordResponse(
                entity.getId().toString(),
                entity.getRecordCode(),
                entity.getDoctorName(),
                entity.getDepartment(),
                entity.getDiseaseSummary(),
                entity.getPrescription(),
                entity.getVisitDate(),
                entity.getAppointmentDate(),
                entity.getCheckinTime(),
                tests,
                entity.getNotes()
        );
    }

    private List<String> parseTests(String testsJson) {
        if (testsJson == null || testsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(testsJson, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Generate a unique 6-digit record code.
     */
    private String generateRecordCode() {
        long count = recordRepository.count() + 1;
        return String.format("%06d", count);
    }
}
