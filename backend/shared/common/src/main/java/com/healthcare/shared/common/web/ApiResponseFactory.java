package com.healthcare.shared.common.web;

import com.healthcare.shared.api.ApiErrorResponse;
import com.healthcare.shared.api.ApiMeta;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;

import java.time.Instant;
import java.util.List;

public final class ApiResponseFactory {
    private ApiResponseFactory() {
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, meta());
    }

    public static ApiErrorResponse error(ErrorCode errorCode, String message, List<String> details) {
        return new ApiErrorResponse(false, errorCode.name(), message, List.copyOf(details), meta());
    }

    private static ApiMeta meta() {
        return new ApiMeta(RequestContext.getRequestId(), Instant.now());
    }
}

