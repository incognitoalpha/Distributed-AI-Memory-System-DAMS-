package com.aimemory.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for token refresh.
 *
 * @author agent
 * @since 1.0.0
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}