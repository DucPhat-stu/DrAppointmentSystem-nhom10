package com.healthcare.ai.web;

import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.common.web.BaseApiExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AIServiceExceptionHandler extends BaseApiExceptionHandler {
    public AIServiceExceptionHandler(ApiResponseFactory apiResponseFactory) {
        super(apiResponseFactory);
    }
}
