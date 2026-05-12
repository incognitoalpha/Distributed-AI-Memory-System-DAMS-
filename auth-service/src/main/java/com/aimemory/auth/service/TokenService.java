package com.aimemory.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service for JWT token issuance and validation.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final SecretKey secretKey;
    private final long jwtExpirySeconds;

    public TokenService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiry-seconds:3600}") long jwtExpirySeconds) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirySeconds = jwtExpirySeconds;
    }

    /**
     * Generates a JWT token for the given tenant and user.
     *
     * @param tenantId the tenant ID
     * @param userId   the user ID
     * @param roles    the user's roles
     * @return the JWT token string
     */
    public String generateToken(UUID tenantId, UUID userId, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtExpirySeconds);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .issuer("auth-service")
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a JWT token and extracts claims.
     *
     * @param token the JWT token to validate
     * @return the token claims if valid
     * @throws JwtException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts tenant ID from token claims.
     */
    public UUID extractTenantId(Claims claims) {
        String tenantId = claims.get("tenantId", String.class);
        return UUID.fromString(tenantId);
    }

    /**
     * Extracts user ID from token subject.
     */
    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extracts roles from token claims.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        return claims.get("roles", List.class);
    }

    /**
     * Checks if a token is expired.
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(Date.from(Instant.now()));
    }

    /**
     * Refreshes an existing token, returning a new token with same claims.
     *
     * @param token the existing token
     * @return a new token with extended expiry
     */
    public String refreshToken(String token) {
        Claims claims = validateToken(token);
        UUID tenantId = extractTenantId(claims);
        UUID userId = extractUserId(claims);
        List<String> roles = extractRoles(claims);

        log.info("Refreshing token for userId={}", userId);
        return generateToken(tenantId, userId, roles);
    }
}