package com.aimemory.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;
    private static final String SECRET = "this-is-a-32-character-secret-key!!";
    private static final long EXPIRY = 3600;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(SECRET, EXPIRY);
    }

    @Test
    void generate_token_creates_valid_jwt() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER", "ROLE_AGENT");

        String token = tokenService.generateToken(tenantId, userId, roles);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void validate_token_returns_claims_for_valid_token() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = tokenService.generateToken(tenantId, userId, List.of("ROLE_USER"));

        Claims claims = tokenService.validateToken(token);

        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    void validate_token_throws_for_invalid_token() {
        assertThrows(JwtException.class, () -> {
            tokenService.validateToken("invalid.token.here");
        });
    }

    @Test
    void validate_token_throws_for_tampered_token() {
        String token = tokenService.generateToken(UUID.randomUUID(), UUID.randomUUID(), List.of("ROLE_USER"));
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        assertThrows(JwtException.class, () -> {
            tokenService.validateToken(tamperedToken);
        });
    }

    @Test
    void extract_tenant_id_returns_correct_value() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = tokenService.generateToken(tenantId, userId, List.of("ROLE_USER"));

        Claims claims = tokenService.validateToken(token);
        UUID extractedTenantId = tokenService.extractTenantId(claims);

        assertEquals(tenantId, extractedTenantId);
    }

    @Test
    void extract_user_id_returns_correct_value() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = tokenService.generateToken(tenantId, userId, List.of("ROLE_USER"));

        Claims claims = tokenService.validateToken(token);
        UUID extractedUserId = tokenService.extractUserId(claims);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void extract_roles_returns_correct_value() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = tokenService.generateToken(tenantId, userId, roles);

        Claims claims = tokenService.validateToken(token);
        List<String> extractedRoles = tokenService.extractRoles(claims);

        assertEquals(roles, extractedRoles);
    }

    @Test
    void is_token_expired_returns_false_for_valid_token() {
        String token = tokenService.generateToken(UUID.randomUUID(), UUID.randomUUID(), List.of("ROLE_USER"));

        Claims claims = tokenService.validateToken(token);
        assertFalse(tokenService.isTokenExpired(claims));
    }

    @Test
    void refresh_token_creates_new_token_with_same_claims() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER");
        String originalToken = tokenService.generateToken(tenantId, userId, roles);

        String newToken = tokenService.refreshToken(originalToken);

        assertNotNull(newToken);

        Claims claims = tokenService.validateToken(newToken);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(tenantId.toString(), claims.get("tenantId", String.class));
    }

    @Test
    void tokens_for_different_users_are_different() {
        UUID tenantId = UUID.randomUUID();
        String token1 = tokenService.generateToken(tenantId, UUID.randomUUID(), List.of("ROLE_USER"));
        String token2 = tokenService.generateToken(tenantId, UUID.randomUUID(), List.of("ROLE_USER"));

        assertFalse(token1.equals(token2));
    }
}