package com.healthcare.user.web;

import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.ForwardedHeaders;
import com.healthcare.user.dto.DoctorCertificationRequest;
import com.healthcare.user.dto.DoctorCertificationResponse;
import com.healthcare.user.service.DoctorCertificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/certifications")
public class DoctorCertificationController {
    private final DoctorCertificationService certificationService;
    private final ApiResponseFactory apiResponseFactory;

    public DoctorCertificationController(DoctorCertificationService certificationService,
                                         ApiResponseFactory apiResponseFactory) {
        this.certificationService = certificationService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<DoctorCertificationResponse>> list(HttpServletRequest request) {
        return apiResponseFactory.success("Certifications loaded", certificationService.list(userId(request)));
    }

    @PostMapping
    public ApiResponse<DoctorCertificationResponse> create(HttpServletRequest request,
                                                           @Valid @RequestBody DoctorCertificationRequest body) {
        return apiResponseFactory.success("Certification created", certificationService.create(userId(request), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<DoctorCertificationResponse> update(HttpServletRequest request,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody DoctorCertificationRequest body) {
        return apiResponseFactory.success("Certification updated", certificationService.update(userId(request), id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable UUID id) {
        certificationService.delete(userId(request), id);
        return apiResponseFactory.success("Certification deleted", null);
    }

    private UUID userId(HttpServletRequest request) {
        return UUID.fromString((String) request.getAttribute(ForwardedHeaders.USER_ID));
    }
}
