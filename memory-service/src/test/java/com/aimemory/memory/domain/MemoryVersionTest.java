package com.aimemory.memory.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MemoryVersionTest {

    @Test
    void memoryVersion_settersAndGettersWorkCorrectly() {
        // Arrange
        MemoryVersion version = new MemoryVersion();
        UUID versionId = UUID.randomUUID();
        UUID memoryId = UUID.randomUUID();
        UUID modifiedBy = UUID.randomUUID();
        String content = "Original memory content";
        String changeReason = "Initial creation";
        Instant now = Instant.now();

        // Act
        version.setVersionId(versionId);
        version.setMemoryId(memoryId);
        version.setVersion(1);
        version.setContent(content);
        version.setChangeReason(changeReason);
        version.setModifiedBy(modifiedBy);

        // Assert
        assertEquals(versionId, version.getVersionId());
        assertEquals(memoryId, version.getMemoryId());
        assertEquals(1, version.getVersion());
        assertEquals(content, version.getContent());
        assertEquals(changeReason, version.getChangeReason());
        assertEquals(modifiedBy, version.getModifiedBy());
    }

    @Test
    void memoryVersion_updateVersion() {
        // Arrange
        MemoryVersion version = new MemoryVersion();

        // Act
        version.setVersion(2);
        version.setContent("Updated content");
        version.setChangeReason("Content update");

        // Assert
        assertEquals(2, version.getVersion());
        assertEquals("Updated content", version.getContent());
        assertEquals("Content update", version.getChangeReason());
    }

    @Test
    void memoryVersion_supersededVersion() {
        // Arrange
        MemoryVersion version = new MemoryVersion();

        // Act
        version.setVersion(3);
        version.setChangeReason("Superseded by version 4");

        // Assert
        assertEquals(3, version.getVersion());
        assertEquals("Superseded by version 4", version.getChangeReason());
    }
}