package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.DoctorLeaveRequest;
import com.healthcare.doctor.dto.DoctorLeaveResponse;
import com.healthcare.doctor.security.CurrentDoctorResolver;
import com.healthcare.doctor.service.DoctorLeaveService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors/leaves")
public class DoctorLeaveController {
    private final DoctorLeaveService leaveService;
    private final CurrentDoctorResolver currentDoctorResolver;
    private final ApiResponseFactory apiResponseFactory;

    public DoctorLeaveController(DoctorLeaveService leaveService,
                                 CurrentDoctorResolver currentDoctorResolver,
                                 ApiResponseFactory apiResponseFactory) {
        this.leaveService = leaveService;
        this.currentDoctorResolver = currentDoctorResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    public ApiResponse<DoctorLeaveResponse> create(HttpServletRequest request,
                                                   @Valid @RequestBody DoctorLeaveRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Leave request created", leaveService.create(doctor.userId(), body));
    }

    @GetMapping
    public ApiResponse<List<DoctorLeaveResponse>> list(HttpServletRequest request) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Leave requests loaded", leaveService.listDoctorLeaves(doctor.userId()));
    }
}
