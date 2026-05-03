package com.healthcare.user.service;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.user.dto.DoctorCertificationRequest;
import com.healthcare.user.dto.DoctorCertificationResponse;
import com.healthcare.user.entity.DoctorCertificationEntity;
import com.healthcare.user.repository.DoctorCertificationJpaRepository;
import com.healthcare.user.repository.UserProfileJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DoctorCertificationService {
    private final DoctorCertificationJpaRepository certificationRepository;
    private final UserProfileJpaRepository userProfileRepository;

    public DoctorCertificationService(DoctorCertificationJpaRepository certificationRepository,
                                      UserProfileJpaRepository userProfileRepository) {
        this.certificationRepository = certificationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorCertificationResponse> list(UUID userId) {
        ensureProfile(userId);
        return certificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoctorCertificationResponse create(UUID userId, DoctorCertificationRequest request) {
        ensureProfile(userId);
        DoctorCertificationEntity entity = new DoctorCertificationEntity();
        entity.setUserId(userId);
        apply(entity, request);
        return toResponse(certificationRepository.save(entity));
    }

    @Transactional
    public DoctorCertificationResponse update(UUID userId, UUID certificationId, DoctorCertificationRequest request) {
        DoctorCertificationEntity entity = certificationRepository.findByIdAndUserId(certificationId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Certification not found"));
        apply(entity, request);
        return toResponse(certificationRepository.save(entity));
    }

    @Transactional
    public void delete(UUID userId, UUID certificationId) {
        DoctorCertificationEntity entity = certificationRepository.findByIdAndUserId(certificationId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Certification not found"));
        certificationRepository.delete(entity);
    }

    private void ensureProfile(UUID userId) {
        if (userProfileRepository.findByUserId(userId).isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Profile not found");
        }
    }

    private void apply(DoctorCertificationEntity entity, DoctorCertificationRequest request) {
        entity.setName(request.name().trim());
        entity.setIssuingAuthority(blankToNull(request.issuingAuthority()));
        entity.setIssueDate(request.issueDate());
        entity.setExpiryDate(request.expiryDate());
        entity.setDocumentUrl(blankToNull(request.documentUrl()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private DoctorCertificationResponse toResponse(DoctorCertificationEntity entity) {
        return new DoctorCertificationResponse(
                entity.getId().toString(),
                entity.getName(),
                entity.getIssuingAuthority(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getDocumentUrl(),
                entity.getCreatedAt()
        );
    }
}
