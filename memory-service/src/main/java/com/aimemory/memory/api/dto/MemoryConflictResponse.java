package com.aimemory.memory.api.dto;

import com.aimemory.memory.domain.MemoryConflict;
import com.aimemory.memory.domain.enums.ConflictResolutionStrategy;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for memory conflict information.
 *
 * @author agent
 * @since 1.0.0
 */
public record MemoryConflictResponse(
        UUID conflictId,
        UUID tenantId,
        UUID oldMemoryId,
        UUID newMemoryId,
        double similarityScore,
        ConflictResolutionStrategy resolutionStrategy,
        String resolutionDetails,
        boolean resolved,
        Instant createdAt
) {
    public static MemoryConflictResponse from(MemoryConflict conflict) {
        return new MemoryConflictResponse(
                conflict.getConflictId(),
                conflict.getTenantId(),
                conflict.getOldMemoryId(),
                conflict.getNewMemoryId(),
                conflict.getSimilarityScore(),
                conflict.getResolutionStrategy(),
                conflict.getResolutionDetails(),
                conflict.isResolved(),
                conflict.getCreatedAt()
        );
    }
}