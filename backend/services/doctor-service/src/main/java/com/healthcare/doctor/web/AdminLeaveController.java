package com.healthcare.doctor.web;

import com.healthcare.doctor.domain.LeaveStatus;
import com.healthcare.doctor.dto.DoctorLeavePageResponse;
import com.healthcare.doctor.dto.DoctorLeaveResponse;
import com.healthcare.doctor.dto.LeaveDecisionRequest;
import com.healthcare.doctor.security.CurrentAdminResolver;
import com.healthcare.doctor.service.DoctorLeaveService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/leaves")
public class AdminLeaveController {
    private final DoctorLeaveService leaveService;
    private final CurrentAdminResolver currentAdminResolver;
    private final ApiResponseFactory apiResponseFactory;

    public AdminLeaveController(DoctorLeaveService leaveService,
                                CurrentAdminResolver currentAdminResolver,
                                ApiResponseFactory apiResponseFactory) {
        this.leaveService = leaveService;
        this.currentAdminResolver = currentAdminResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<DoctorLeavePageResponse> list(
            HttpServletRequest request,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        currentAdminResolver.resolve(request);
        return apiResponseFactory.success("Leave requests loaded", leaveService.listAdminLeaves(status, page, size));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<DoctorLeaveResponse> approve(HttpServletRequest request, @PathVariable UUID id) {
        AuthenticatedUser admin = currentAdminResolver.resolve(request);
        return apiResponseFactory.success("Leave request approved", leaveService.approve(admin.userId(), id));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<DoctorLeaveResponse> reject(HttpServletRequest request,
                                                   @PathVariable UUID id,
                                                   @Valid @RequestBody(required = false) LeaveDecisionRequest body) {
        AuthenticatedUser admin = currentAdminResolver.resolve(request);
        return apiResponseFactory.success("Leave request rejected", leaveService.reject(admin.userId(), id, body));
    }
}
