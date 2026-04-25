package com.healthcare.doctor.dto;

import java.util.List;

public record DoctorLeavePageResponse(
        List<DoctorLeaveResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
