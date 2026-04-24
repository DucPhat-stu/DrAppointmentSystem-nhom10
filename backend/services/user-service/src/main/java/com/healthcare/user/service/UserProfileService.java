package com.healthcare.user.service;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.user.dto.UpdateProfileRequest;
import com.healthcare.user.dto.UserProfileResponse;
import com.healthcare.user.entity.UserProfileEntity;
import com.healthcare.user.repository.UserProfileJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserProfileService {
    private final UserProfileJpaRepository userProfileRepository;

    public UserProfileService(UserProfileJpaRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        UserProfileEntity entity = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Profile not found for user: " + userId));
        return toResponse(entity);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfileEntity entity = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Profile not found for user: " + userId));

        entity.setFullName(request.fullName());
        if (request.phone() != null) entity.setPhone(request.phone());
        if (request.address() != null) entity.setAddress(request.address());
        if (request.dateOfBirth() != null) entity.setDateOfBirth(request.dateOfBirth());
        if (request.gender() != null) entity.setGender(request.gender());
        if (request.emergencyContact() != null) entity.setEmergencyContact(request.emergencyContact());
        entity.setUpdatedAt(OffsetDateTime.now());

        userProfileRepository.save(entity);
        return toResponse(entity);
    }

    private UserProfileResponse toResponse(UserProfileEntity entity) {
        return new UserProfileResponse(
                entity.getId().toString(),
                entity.getUserId().toString(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getDateOfBirth(),
                entity.getGender(),
                entity.getEmergencyContact(),
                entity.getAvatarUrl()
        );
    }
}
