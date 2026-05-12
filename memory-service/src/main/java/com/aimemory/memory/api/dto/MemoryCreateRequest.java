package com.aimemory.memory.api.dto;

import com.aimemory.memory.domain.enums.MemoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating a new memory.
 *
 * @author agent
 * @since 1.0.0
 */
public record MemoryCreateRequest(
        @NotBlank(message = "Content is required")
        String content,

        @NotNull(message = "Memory type is required")
        MemoryType memoryType,

        @NotNull(message = "Source conversation ID is required")
        UUID sourceConversationId,

        @NotNull(message = "Source session ID is required")
        UUID sourceSessionId
) {
}