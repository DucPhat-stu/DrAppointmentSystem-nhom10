package com.healthcare.ai.security;

import com.healthcare.ai.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

public class RateLimitFilter extends OncePerRequestFilter {
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String GLOBAL_KEY = "global";

    private final RateLimitProperties properties;
    private final FixedWindowRateLimiter limiter;

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
        this.limiter = new FixedWindowRateLimiter(Clock.systemUTC());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled() || !HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();
        return !path.equals("/api/v1/ai/check")
                && !path.equals("/api/v1/ai/check/structured")
                && !path.equals("/api/v1/ai/preview/structured");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!limiter.tryAcquire(GLOBAL_KEY, properties.getGlobalPerMinute(), WINDOW)) {
            reject(response, limiter.retryAfterSeconds(GLOBAL_KEY, WINDOW));
            return;
        }

        String userKey = "user:" + userIdentity(request);
        if (!limiter.tryAcquire(userKey, properties.getPerUserPerMinute(), WINDOW)) {
            reject(response, limiter.retryAfterSeconds(userKey, WINDOW));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static String userIdentity(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        return request.getRemoteAddr();
    }

    private static void reject(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        response.getWriter().write("""
                {"success":false,"code":"TOO_MANY_REQUESTS","message":"Too many AI requests. Please try again later."}""");
    }
}
