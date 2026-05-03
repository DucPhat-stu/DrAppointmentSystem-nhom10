package com.healthcare.user.service;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.user.dto.UpdateProfileRequest;
import com.healthcare.user.dto.UserProfileResponse;
import com.healthcare.user.entity.UserProfileEntity;
import com.healthcare.user.repository.UserProfileJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getProfiles(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userProfileRepository.findAllByUserIdIn(userIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserProfileResponse updateAvatar(UUID userId, MultipartFile file) {
        // Validate file not empty
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Avatar file is required");
        }

        // Validate file size (max 2MB)
        long maxSize = 2L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Avatar file size must not exceed 2MB");
        }

        // Validate MIME type (must be image/*)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Avatar must be an image file (image/*)");
        }

        // Validate file extension whitelist
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Avatar file name is required");
        }

        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".webp", ".gif"};
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot).toLowerCase();
        }

        String finalExtension = extension;
        boolean isValidExtension = java.util.Arrays.stream(allowedExtensions)
                .anyMatch(ext -> ext.equals(finalExtension));

        if (!isValidExtension) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Avatar file extension must be one of: jpg, jpeg, png, webp, gif");
        }

        UserProfileEntity entity = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Profile not found for user: " + userId));

        // Server-generated filename (never trust original filename)
        String fileName = userId + "-" + System.currentTimeMillis() + extension;
        Path uploadDir = Path.of("uploads", "avatars").toAbsolutePath().normalize();
        Path target = uploadDir.resolve(fileName).normalize();
        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Unable to store avatar");
        }

        entity.setAvatarUrl("/uploads/avatars/" + fileName);
        entity.setUpdatedAt(OffsetDateTime.now());
        userProfileRepository.save(entity);
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
        if (request.specialty() != null) entity.setSpecialty(request.specialty());
        if (request.department() != null) entity.setDepartment(request.department());
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
                entity.getAvatarUrl(),
                entity.getSpecialty(),
                entity.getDepartment()
        );
    }
}
