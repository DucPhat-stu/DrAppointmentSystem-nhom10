package com.healthcare.doctor.security;

import com.healthcare.doctor.config.InternalServiceProperties;
import com.healthcare.shared.security.ForwardedHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class InternalServiceTokenFilter extends OncePerRequestFilter {
    public static final String HEADER_NAME = "X-Internal-Service-Token";
    private final InternalServiceProperties properties;

    public InternalServiceTokenFilter(InternalServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String expected = properties.getServiceToken();
        String provided = request.getHeader(HEADER_NAME);
        if (expected == null || expected.isBlank() || provided == null || provided.isBlank()
                || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"success":false,"code":"UNAUTHORIZED","message":"Invalid internal service token"}""");
            return;
        }

        request.setAttribute(ForwardedHeaders.USER_ID, "00000000-0000-0000-0000-000000000000");
        request.setAttribute(ForwardedHeaders.USER_ROLE, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
        ));
        filterChain.doFilter(request, response);
    }
}
