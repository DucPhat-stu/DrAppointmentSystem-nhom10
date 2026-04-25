package com.healthcare.doctor.client;

import com.healthcare.doctor.config.UserClientProperties;
import com.healthcare.doctor.dto.PatientMedicalRecordResponse;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.UUID;

@Component
public class RestUserMedicalRecordClient implements UserMedicalRecordClient {
    private static final int MAX_ATTEMPTS = 3;
    private static final ParameterizedTypeReference<ApiResponse<List<PatientMedicalRecordResponse>>> RECORDS_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient userRestClient;
    private final UserClientProperties properties;

    public RestUserMedicalRecordClient(RestClient userRestClient, UserClientProperties properties) {
        this.userRestClient = userRestClient;
        this.properties = properties;
    }

    @Override
    public List<PatientMedicalRecordResponse> findPatientMedicalRecords(UUID patientId) {
        return handle(() -> userRestClient.get()
                .uri("/internal/patients/{patientId}/medical-records", patientId)
                .header("X-Internal-Service-Token", properties.getServiceToken())
                .retrieve()
                .body(RECORDS_TYPE)
                .data());
    }

    private <T> T handle(RemoteCall<T> call) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.execute();
            } catch (RestClientResponseException exception) {
                if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Patient medical records not found");
                }
                throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service returned an error");
            } catch (RestClientException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service is unavailable");
                }
                backoff(attempt);
            }
        }
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service is unavailable");
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service retry was interrupted");
        }
    }

    private interface RemoteCall<T> {
        T execute();
    }
}
