package com.healthcare.user.web;

import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.common.web.BaseApiExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserServiceExceptionHandler extends BaseApiExceptionHandler {
    public UserServiceExceptionHandler(ApiResponseFactory apiResponseFactory) {
        super(apiResponseFactory);
    }
}

