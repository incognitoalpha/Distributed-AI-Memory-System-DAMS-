package com.aimemory.memory.domain;

import com.aimemory.memory.domain.enums.MemoryType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    @Test
    void memory_settersAndGettersWorkCorrectly() {
        // Arrange
        Memory memory = new Memory();
        UUID memoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String content = "Test memory content";
        Instant now = Instant.now();

        // Act
        memory.setMemoryId(memoryId);
        memory.setTenantId(tenantId);
        memory.setUserId(userId);
        memory.setContent(content);
        memory.setMemoryType(MemoryType.EPISODIC);
        memory.setSourceConversationId(conversationId);
        memory.setSourceSessionId(sessionId);
        memory.setVersion(1);
        memory.setReplacesMemoryId(null);
        memory.setEmbeddingModelVersion("text-embedding-3-small-v1");
        memory.setEmbeddingDimension(1536);
        memory.setImportanceScore(0.5);
        memory.setRetrievalCount(10);
        memory.setLastRetrievedAt(now);
        memory.setSoftDeleted(false);
        memory.setSoftDeletedAt(null);
        memory.setHardDeleteEligibleAt(null);

        // Assert
        assertEquals(memoryId, memory.getMemoryId());
        assertEquals(tenantId, memory.getTenantId());
        assertEquals(userId, memory.getUserId());
        assertEquals(content, memory.getContent());
        assertEquals(MemoryType.EPISODIC, memory.getMemoryType());
        assertEquals(conversationId, memory.getSourceConversationId());
        assertEquals(sessionId, memory.getSourceSessionId());
        assertEquals(1, memory.getVersion());
        assertNull(memory.getReplacesMemoryId());
        assertEquals("text-embedding-3-small-v1", memory.getEmbeddingModelVersion());
        assertEquals(1536, memory.getEmbeddingDimension());
        assertEquals(0.5, memory.getImportanceScore());
        assertEquals(10, memory.getRetrievalCount());
        assertEquals(now, memory.getLastRetrievedAt());
        assertFalse(memory.isSoftDeleted());
    }

    @Test
    void memory_softDeleteSetsDates() {
        // Arrange
        Memory memory = new Memory();
        Instant now = Instant.now();

        // Act
        memory.setSoftDeleted(true);
        memory.setSoftDeletedAt(now);
        memory.setHardDeleteEligibleAt(now.plusSeconds(30L * 24 * 60 * 60));

        // Assert
        assertTrue(memory.isSoftDeleted());
        assertEquals(now, memory.getSoftDeletedAt());
        assertNotNull(memory.getHardDeleteEligibleAt());
    }

    @Test
    void memory_versionUpdate() {
        // Arrange
        Memory memory = new Memory();
        UUID oldMemoryId = UUID.randomUUID();
        UUID newMemoryId = UUID.randomUUID();

        // Act
        memory.setVersion(2);
        memory.setReplacesMemoryId(oldMemoryId);

        // Assert
        assertEquals(2, memory.getVersion());
        assertEquals(oldMemoryId, memory.getReplacesMemoryId());
    }

    @Test
    void memory_semanticType() {
        // Arrange
        Memory memory = new Memory();

        // Act
        memory.setMemoryType(MemoryType.SEMANTIC);

        // Assert
        assertEquals(MemoryType.SEMANTIC, memory.getMemoryType());
    }

    @Test
    void memory_importanceScoreRange() {
        // Arrange
        Memory memory = new Memory();

        // Act
        memory.setImportanceScore(0.0);
        assertEquals(0.0, memory.getImportanceScore(), 0.001);

        memory.setImportanceScore(1.0);
        assertEquals(1.0, memory.getImportanceScore(), 0.001);

        memory.setImportanceScore(0.75);
        assertEquals(0.75, memory.getImportanceScore(), 0.001);
    }

    @Test
    void memory_retrievalCount() {
        // Arrange
        Memory memory = new Memory();

        // Act
        memory.setRetrievalCount(0);
        assertEquals(0, memory.getRetrievalCount());

        memory.setRetrievalCount(100);
        assertEquals(100, memory.getRetrievalCount());
    }
}