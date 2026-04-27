package com.healthcare.shared.common.web;

import com.healthcare.shared.security.ForwardedHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestContextFilter extends OncePerRequestFilter {
    private final RequestMetadataContext requestMetadataContext;

    public RequestContextFilter(RequestMetadataContext requestMetadataContext) {
        this.requestMetadataContext = requestMetadataContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(ForwardedHeaders.REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        requestMetadataContext.open(requestId);
        MDC.put("traceId", requestId);
        putIfPresent("userId", request.getHeader(ForwardedHeaders.USER_ID));
        putIfPresent("role", request.getHeader(ForwardedHeaders.USER_ROLE));
        response.setHeader(ForwardedHeaders.REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("role");
            MDC.remove("userId");
            MDC.remove("traceId");
            requestMetadataContext.clear();
        }
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }
}
