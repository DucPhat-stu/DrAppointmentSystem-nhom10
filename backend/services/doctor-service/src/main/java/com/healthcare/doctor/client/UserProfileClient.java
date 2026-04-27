package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.DoctorProfileSummaryResponse;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserProfileClient {
    List<DoctorProfileSummaryResponse> findProfiles(Collection<UUID> userIds);
}
