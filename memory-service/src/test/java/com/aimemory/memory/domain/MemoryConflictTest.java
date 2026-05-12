package com.aimemory.memory.domain;

import com.aimemory.memory.domain.enums.ConflictResolutionStrategy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MemoryConflictTest {

    @Test
    void memoryConflict_settersAndGettersWorkCorrectly() {
        // Arrange
        MemoryConflict conflict = new MemoryConflict();
        UUID conflictId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID oldMemoryId = UUID.randomUUID();
        UUID newMemoryId = UUID.randomUUID();

        // Act
        conflict.setConflictId(conflictId);
        conflict.setTenantId(tenantId);
        conflict.setOldMemoryId(oldMemoryId);
        conflict.setNewMemoryId(newMemoryId);
        conflict.setSimilarityScore(0.92);
        conflict.setResolutionStrategy(ConflictResolutionStrategy.LATEST_WINS);
        conflict.setResolutionDetails("Auto-resolved using latest wins");
        conflict.setResolved(false);

        // Assert
        assertEquals(conflictId, conflict.getConflictId());
        assertEquals(tenantId, conflict.getTenantId());
        assertEquals(oldMemoryId, conflict.getOldMemoryId());
        assertEquals(newMemoryId, conflict.getNewMemoryId());
        assertEquals(0.92, conflict.getSimilarityScore(), 0.001);
        assertEquals(ConflictResolutionStrategy.LATEST_WINS, conflict.getResolutionStrategy());
        assertEquals("Auto-resolved using latest wins", conflict.getResolutionDetails());
        assertFalse(conflict.isResolved());
    }

    @Test
    void memoryConflict_resolvedState() {
        // Arrange
        MemoryConflict conflict = new MemoryConflict();

        // Act
        conflict.setResolved(true);

        // Assert
        assertTrue(conflict.isResolved());
    }

    @Test
    void memoryConflict_mergeStrategy() {
        // Arrange
        MemoryConflict conflict = new MemoryConflict();

        // Act
        conflict.setResolutionStrategy(ConflictResolutionStrategy.MERGE);

        // Assert
        assertEquals(ConflictResolutionStrategy.MERGE, conflict.getResolutionStrategy());
    }
}