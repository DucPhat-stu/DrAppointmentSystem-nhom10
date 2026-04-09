package com.healthcare.shared.common.web;

import com.healthcare.shared.api.ApiErrorResponse;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;

import java.util.List;

public class ApiResponseFactory {
    private final RequestMetadataProvider requestMetadataProvider;

    public ApiResponseFactory(RequestMetadataProvider requestMetadataProvider) {
        this.requestMetadataProvider = requestMetadataProvider;
    }

    public <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, meta());
    }

    public ApiErrorResponse error(ErrorCode errorCode, String message, List<String> details) {
        return new ApiErrorResponse(false, errorCode.name(), message, List.copyOf(details), meta());
    }

    private com.healthcare.shared.api.ApiMeta meta() {
        return requestMetadataProvider.current();
    }
}
