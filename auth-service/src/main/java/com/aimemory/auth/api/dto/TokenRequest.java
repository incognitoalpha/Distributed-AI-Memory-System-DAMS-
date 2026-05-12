package com.aimemory.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for token issuance.
 *
 * @author agent
 * @since 1.0.0
 */
public record TokenRequest(
        @NotNull(message = "Tenant ID is required")
        UUID tenantId,

        @NotNull(message = "User ID is required")
        UUID userId,

        List<String> roles
) {
}