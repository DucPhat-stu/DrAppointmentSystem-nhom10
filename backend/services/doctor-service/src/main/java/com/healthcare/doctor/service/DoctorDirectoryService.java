package com.healthcare.doctor.service;

import com.healthcare.doctor.client.UserProfileClient;
import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.dto.DoctorAvailabilityResponse;
import com.healthcare.doctor.dto.DoctorProfileSummaryResponse;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoctorDirectoryService {
    private final TimeSlotJpaRepository timeSlotRepository;
    private final UserProfileClient userProfileClient;

    public DoctorDirectoryService(TimeSlotJpaRepository timeSlotRepository,
                                  UserProfileClient userProfileClient) {
        this.timeSlotRepository = timeSlotRepository;
        this.userProfileClient = userProfileClient;
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> findAvailableDoctors(LocalDate date) {
        List<TimeSlotJpaRepository.DoctorAvailabilityProjection> availability =
                timeSlotRepository.findDoctorAvailabilityByDate(date, TimeSlotStatus.AVAILABLE);
        Map<UUID, DoctorProfileSummaryResponse> profiles = loadProfiles(
                availability.stream().map(TimeSlotJpaRepository.DoctorAvailabilityProjection::getDoctorId).toList()
        );

        return availability.stream()
                .map(item -> toResponse(item, date, profiles.get(item.getDoctorId())))
                .toList();
    }

    private Map<UUID, DoctorProfileSummaryResponse> loadProfiles(Collection<UUID> doctorIds) {
        try {
            return userProfileClient.findProfiles(doctorIds)
                    .stream()
                    .collect(Collectors.toMap(DoctorProfileSummaryResponse::userId, Function.identity()));
        } catch (ApiException exception) {
            return Map.of();
        }
    }

    private DoctorAvailabilityResponse toResponse(TimeSlotJpaRepository.DoctorAvailabilityProjection item,
                                                  LocalDate date,
                                                  DoctorProfileSummaryResponse profile) {
        UUID doctorId = item.getDoctorId();
        return new DoctorAvailabilityResponse(
                doctorId,
                profile != null ? profile.fullName() : "Doctor " + doctorId.toString().substring(0, 8),
                profile != null ? profile.specialty() : null,
                profile != null ? profile.department() : null,
                profile != null ? profile.avatarUrl() : null,
                date,
                item.getAvailableSlots()
        );
    }
}
