package com.healthcare.doctor.dto;

import java.util.List;

public record AdminDoctorPageResponse(
        List<DoctorProfileSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
