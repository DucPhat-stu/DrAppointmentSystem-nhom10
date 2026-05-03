package com.healthcare.auth.security;

import com.healthcare.auth.config.JwtProperties;
import com.healthcare.shared.security.ForwardedHeaders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT validation filter for auth-service admin endpoints.
 * Only validates the token (does not issue it — that is AuthController's job).
 * Sets USER_ID / USER_ROLE request attributes consumed by AdminUserController.
 */
public class JwtValidationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    public JwtValidationFilter(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(authHeader.substring(7))
                    .getPayload();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            request.setAttribute(ForwardedHeaders.USER_ID, userId);
            request.setAttribute(ForwardedHeaders.USER_ROLE, role);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"success":false,"errorCode":"UNAUTHORIZED","message":"Invalid or expired token"}""");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
