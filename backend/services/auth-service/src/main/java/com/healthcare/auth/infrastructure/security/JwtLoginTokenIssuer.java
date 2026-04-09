package com.healthcare.auth.infrastructure.security;

import com.healthcare.auth.application.IssuedTokenPair;
import com.healthcare.auth.application.LoginTokenIssuer;
import com.healthcare.auth.application.RolePermissionMapper;
import com.healthcare.auth.application.UserCredential;
import com.healthcare.auth.config.JwtProperties;
import com.healthcare.shared.security.Permission;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtLoginTokenIssuer implements LoginTokenIssuer {
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final RolePermissionMapper rolePermissionMapper;

    public JwtLoginTokenIssuer(JwtProperties jwtProperties, Clock clock, RolePermissionMapper rolePermissionMapper) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public IssuedTokenPair issue(UserCredential userCredential) {
        Instant issuedAt = clock.instant();
        Instant accessExpiresAt = issuedAt.plusSeconds(jwtProperties.getAccessTokenExpiresInSeconds());
        Instant refreshExpiresAt = issuedAt.plusSeconds(jwtProperties.getRefreshTokenExpiresInSeconds());
        Set<Permission> permissions = rolePermissionMapper.map(userCredential.role());

        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        String accessToken = Jwts.builder()
                .subject(userCredential.userId().toString())
                .claim("email", userCredential.email())
                .claim("role", userCredential.role().name())
                .claim("permissions", permissions.stream().map(Enum::name).toList())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(accessExpiresAt))
                .signWith(secretKey)
                .compact();

        String refreshToken = UUID.randomUUID().toString();

        return new IssuedTokenPair(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenExpiresInSeconds(),
                OffsetDateTime.ofInstant(issuedAt, ZoneOffset.UTC),
                OffsetDateTime.ofInstant(refreshExpiresAt, ZoneOffset.UTC),
                permissions
        );
    }
}
