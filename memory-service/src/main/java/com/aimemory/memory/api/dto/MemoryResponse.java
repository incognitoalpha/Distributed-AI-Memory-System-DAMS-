package com.aimemory.memory.api.dto;

import com.aimemory.memory.domain.Memory;
import com.aimemory.memory.domain.enums.MemoryType;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for memory operations.
 *
 * @author agent
 * @since 1.0.0
 */
public record MemoryResponse(
        UUID memoryId,
        UUID tenantId,
        UUID userId,
        String content,
        MemoryType memoryType,
        UUID sourceConversationId,
        UUID sourceSessionId,
        int version,
        UUID replacesMemoryId,
        String embeddingModelVersion,
        int embeddingDimension,
        double importanceScore,
        long retrievalCount,
        Instant lastRetrievedAt,
        boolean softDeleted,
        Instant softDeletedAt,
        Instant hardDeleteEligibleAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static MemoryResponse from(Memory memory) {
        return new MemoryResponse(
                memory.getMemoryId(),
                memory.getTenantId(),
                memory.getUserId(),
                memory.getContent(),
                memory.getMemoryType(),
                memory.getSourceConversationId(),
                memory.getSourceSessionId(),
                memory.getVersion(),
                memory.getReplacesMemoryId(),
                memory.getEmbeddingModelVersion(),
                memory.getEmbeddingDimension(),
                memory.getImportanceScore(),
                memory.getRetrievalCount(),
                memory.getLastRetrievedAt(),
                memory.isSoftDeleted(),
                memory.getSoftDeletedAt(),
                memory.getHardDeleteEligibleAt(),
                memory.getCreatedAt(),
                memory.getUpdatedAt()
        );
    }
}