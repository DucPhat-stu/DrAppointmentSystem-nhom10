package com.healthcare.doctor.web;

import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.common.web.BaseApiExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DoctorServiceExceptionHandler extends BaseApiExceptionHandler {
    public DoctorServiceExceptionHandler(ApiResponseFactory apiResponseFactory) {
        super(apiResponseFactory);
    }
}

