package com.healthcare.doctor.web;

import com.healthcare.doctor.client.UserProfileClient;
import com.healthcare.doctor.dto.AdminDoctorPageResponse;
import com.healthcare.doctor.dto.DoctorProfileSummaryResponse;
import com.healthcare.doctor.entity.DoctorScheduleEntity;
import com.healthcare.doctor.repository.DoctorScheduleJpaRepository;
import com.healthcare.doctor.security.CurrentAdminResolver;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Admin Doctor Management endpoints.
 * Approve/Disable are intentionally NOT implemented here because doctor-service
 * does not own the doctor profile / account status — that belongs to auth-service (user account)
 * and user-service (profile). The FE calls auth-service /admin/users/{id}/disable for that.
 *
 * This controller exposes a paginated list of all doctors registered in the system
 * (derived from DoctorScheduleEntity which contains doctorId).
 */
@RestController
@RequestMapping("/api/v1/admin/doctors")
public class AdminDoctorController {

    private final DoctorScheduleJpaRepository scheduleRepository;
    private final UserProfileClient userProfileClient;
    private final CurrentAdminResolver currentAdminResolver;
    private final ApiResponseFactory apiResponseFactory;

    public AdminDoctorController(DoctorScheduleJpaRepository scheduleRepository,
                                  UserProfileClient userProfileClient,
                                  CurrentAdminResolver currentAdminResolver,
                                  ApiResponseFactory apiResponseFactory) {
        this.scheduleRepository = scheduleRepository;
        this.userProfileClient = userProfileClient;
        this.currentAdminResolver = currentAdminResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    /**
     * GET /api/v1/admin/doctors
     * Returns a paginated list of doctors (resolved via user-service profiles).
     * Pagination is applied in-memory as doctor IDs come from schedule aggregation.
     */
    @GetMapping
    public ApiResponse<AdminDoctorPageResponse> listDoctors(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        currentAdminResolver.resolve(request);

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        // Collect unique doctorIds from all schedules
        List<UUID> allDoctorIds = scheduleRepository.findAll()
                .stream()
                .map(DoctorScheduleEntity::getDoctorId)
                .distinct()
                .sorted()
                .toList();

        long total = allDoctorIds.size();
        int totalPages = (int) Math.ceil((double) total / safeSize);
        int fromIndex = safePage * safeSize;

        List<UUID> pageIds = fromIndex >= allDoctorIds.size()
                ? List.of()
                : allDoctorIds.subList(fromIndex, Math.min(fromIndex + safeSize, allDoctorIds.size()));

        List<DoctorProfileSummaryResponse> profiles;
        if (pageIds.isEmpty()) {
            profiles = List.of();
        } else {
            try {
                Map<UUID, DoctorProfileSummaryResponse> profileMap = userProfileClient.findProfiles(pageIds)
                        .stream()
                        .collect(Collectors.toMap(DoctorProfileSummaryResponse::userId, Function.identity()));
                profiles = pageIds.stream()
                        .map(id -> profileMap.getOrDefault(id, new DoctorProfileSummaryResponse(
                                id, "Doctor " + id.toString().substring(0, 8), null, null, null, null
                        )))
                        .toList();
            } catch (Exception e) {
                profiles = pageIds.stream()
                        .map(id -> new DoctorProfileSummaryResponse(
                                id, "Doctor " + id.toString().substring(0, 8), null, null, null, null
                        ))
                        .toList();
            }
        }

        return apiResponseFactory.success("Doctors loaded", new AdminDoctorPageResponse(
                profiles, safePage, safeSize, total, totalPages
        ));
    }
}
