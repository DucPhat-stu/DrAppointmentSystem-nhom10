package com.healthcare.auth.web;

import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.common.web.BaseApiExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthServiceExceptionHandler extends BaseApiExceptionHandler {
    public AuthServiceExceptionHandler(ApiResponseFactory apiResponseFactory) {
        super(apiResponseFactory);
    }
}

