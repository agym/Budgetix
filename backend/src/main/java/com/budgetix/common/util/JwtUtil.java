package com.budgetix.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.access-secret}")
    private String accessSecret;

    @Value("${app.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${app.jwt.access-expiry-ms}")
    private long accessExpiryMs;

    @Value("${app.jwt.refresh-expiry-ms}")
    private long refreshExpiryMs;

    public String generateAccessToken(UUID userId, String email, String role) {
        return buildToken(userId, email, role, accessSecret, accessExpiryMs);
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
            .signWith(getKey(refreshSecret))
            .compact();
    }

    private String buildToken(UUID userId, String email, String role, String secret, long expiryMs) {
        return Jwts.builder()
            .subject(userId.toString())
            .claims(Map.of("email", email, "role", role))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiryMs))
            .signWith(getKey(secret))
            .compact();
    }

    public UUID extractUserIdFromAccess(String token) {
        return UUID.fromString(extractClaim(token, accessSecret, Claims::getSubject));
    }

    public UUID extractUserIdFromRefresh(String token) {
        return UUID.fromString(extractClaim(token, refreshSecret, Claims::getSubject));
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, accessSecret);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, refreshSecret);
    }

    private boolean isTokenValid(String token, String secret) {
        try {
            Jwts.parser().verifyWith(getKey(secret)).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private <T> T extractClaim(String token, String secret, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
            .verifyWith(getKey(secret))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public long getAccessExpiryMs() {
        return accessExpiryMs;
    }
}
