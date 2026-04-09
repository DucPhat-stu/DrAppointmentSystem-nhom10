package com.healthcare.shared.common.web;

import com.healthcare.shared.security.ForwardedHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        response.setHeader(ForwardedHeaders.REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            requestMetadataContext.clear();
        }
    }
}
