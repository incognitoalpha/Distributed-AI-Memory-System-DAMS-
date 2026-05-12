package com.aimemory.auth.api.dto;

import java.time.Instant;

/**
 * Response DTO for token operations.
 *
 * @author agent
 * @since 1.0.0
 */
public record TokenResponse(
        String token,
        String tokenType,
        long expiresIn,
        Instant issuedAt
) {
}