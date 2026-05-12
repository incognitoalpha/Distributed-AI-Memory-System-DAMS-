package com.aimemory.memory.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void memoryType_hasTwoValues() {
        // Assert
        assertEquals(2, MemoryType.values().length);
    }

    @Test
    void memoryType_episodicExists() {
        // Act
        MemoryType episodic = MemoryType.valueOf("EPISODIC");

        // Assert
        assertNotNull(episodic);
        assertEquals("EPISODIC", episodic.name());
    }

    @Test
    void memoryType_semanticExists() {
        // Act
        MemoryType semantic = MemoryType.valueOf("SEMANTIC");

        // Assert
        assertNotNull(semantic);
        assertEquals("SEMANTIC", semantic.name());
    }

    @Test
    void conflictResolutionStrategy_hasThreeValues() {
        // Assert
        assertEquals(3, ConflictResolutionStrategy.values().length);
    }

    @Test
    void conflictResolutionStrategy_latestWinsExists() {
        // Act
        ConflictResolutionStrategy strategy = ConflictResolutionStrategy.valueOf("LATEST_WINS");

        // Assert
        assertNotNull(strategy);
        assertEquals("LATEST_WINS", strategy.name());
    }

    @Test
    void conflictResolutionStrategy_mergeExists() {
        // Act
        ConflictResolutionStrategy strategy = ConflictResolutionStrategy.valueOf("MERGE");

        // Assert
        assertNotNull(strategy);
        assertEquals("MERGE", strategy.name());
    }

    @Test
    void conflictResolutionStrategy_manualExists() {
        // Act
        ConflictResolutionStrategy strategy = ConflictResolutionStrategy.valueOf("MANUAL");

        // Assert
        assertNotNull(strategy);
        assertEquals("MANUAL", strategy.name());
    }
}