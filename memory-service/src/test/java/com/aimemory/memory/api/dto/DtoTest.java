package com.aimemory.memory.api.dto;

import com.aimemory.memory.domain.Memory;
import com.aimemory.memory.domain.enums.MemoryType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void memoryCreateRequest_constructorAndGetters() {
        // Arrange
        String content = "Test memory content";
        MemoryType memoryType = MemoryType.EPISODIC;
        UUID conversationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        // Act
        MemoryCreateRequest request = new MemoryCreateRequest(content, memoryType, conversationId, sessionId);

        // Assert
        assertEquals(content, request.content());
        assertEquals(memoryType, request.memoryType());
        assertEquals(conversationId, request.sourceConversationId());
        assertEquals(sessionId, request.sourceSessionId());
    }

    @Test
    void memoryResponse_fromMemory() {
        // Arrange
        Memory memory = new Memory();
        UUID memoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        memory.setMemoryId(memoryId);
        memory.setTenantId(tenantId);
        memory.setUserId(userId);
        memory.setContent("Test content");
        memory.setMemoryType(MemoryType.SEMANTIC);
        memory.setSourceConversationId(conversationId);
        memory.setSourceSessionId(sessionId);
        memory.setVersion(1);
        memory.setReplacesMemoryId(null);
        memory.setEmbeddingModelVersion("text-embedding-3-small-v1");
        memory.setEmbeddingDimension(1536);
        memory.setImportanceScore(0.7);
        memory.setRetrievalCount(5);
        memory.setLastRetrievedAt(Instant.now());
        memory.setSoftDeleted(false);

        // Act
        MemoryResponse response = MemoryResponse.from(memory);

        // Assert
        assertEquals(memoryId, response.memoryId());
        assertEquals(tenantId, response.tenantId());
        assertEquals(userId, response.userId());
        assertEquals("Test content", response.content());
        assertEquals(MemoryType.SEMANTIC, response.memoryType());
        assertEquals(1, response.version());
        assertEquals("text-embedding-3-small-v1", response.embeddingModelVersion());
        assertEquals(1536, response.embeddingDimension());
        assertEquals(0.7, response.importanceScore());
        assertEquals(5, response.retrievalCount());
        assertFalse(response.softDeleted());
    }

    @Test
    void memoryResponse_withNullReplacesMemoryId() {
        // Arrange
        Memory memory = new Memory();
        memory.setMemoryId(UUID.randomUUID());
        memory.setTenantId(UUID.randomUUID());
        memory.setUserId(UUID.randomUUID());
        memory.setContent("Content");
        memory.setMemoryType(MemoryType.EPISODIC);
        memory.setSourceConversationId(UUID.randomUUID());
        memory.setSourceSessionId(UUID.randomUUID());
        memory.setVersion(1);
        memory.setReplacesMemoryId(null); // Explicitly null

        // Act
        MemoryResponse response = MemoryResponse.from(memory);

        // Assert
        assertNull(response.replacesMemoryId());
    }

    @Test
    void memoryResponse_withSoftDeleteDates() {
        // Arrange
        Memory memory = new Memory();
        Instant now = Instant.now();

        memory.setMemoryId(UUID.randomUUID());
        memory.setTenantId(UUID.randomUUID());
        memory.setUserId(UUID.randomUUID());
        memory.setContent("Content");
        memory.setMemoryType(MemoryType.EPISODIC);
        memory.setSourceConversationId(UUID.randomUUID());
        memory.setSourceSessionId(UUID.randomUUID());
        memory.setVersion(1);
        memory.setSoftDeleted(true);
        memory.setSoftDeletedAt(now);
        memory.setHardDeleteEligibleAt(now.plusSeconds(30L * 24 * 60 * 60));

        // Act
        MemoryResponse response = MemoryResponse.from(memory);

        // Assert
        assertTrue(response.softDeleted());
        assertNotNull(response.softDeletedAt());
        assertNotNull(response.hardDeleteEligibleAt());
    }

    @Test
    void memoryCreateRequest_equalsAndHashCode() {
        // Arrange
        UUID conversationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MemoryCreateRequest request1 = new MemoryCreateRequest("content", MemoryType.EPISODIC, conversationId, sessionId);
        MemoryCreateRequest request2 = new MemoryCreateRequest("content", MemoryType.EPISODIC, conversationId, sessionId);

        // Assert
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void memoryResponse_equalsAndHashCode() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MemoryResponse response1 = new MemoryResponse(
                memoryId, tenantId, userId, "content", MemoryType.EPISODIC,
                conversationId, sessionId, 1, null, "v1", 1536, 0.5, 0,
                null, false, null, null, null, null
        );

        MemoryResponse response2 = new MemoryResponse(
                memoryId, tenantId, userId, "content", MemoryType.EPISODIC,
                conversationId, sessionId, 1, null, "v1", 1536, 0.5, 0,
                null, false, null, null, null, null
        );

        // Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
}