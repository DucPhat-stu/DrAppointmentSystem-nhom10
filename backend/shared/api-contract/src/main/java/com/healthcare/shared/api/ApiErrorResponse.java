package com.healthcare.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        boolean success,
        String errorCode,
        String message,
        List<String> details,
        ApiMeta meta
) {
}

