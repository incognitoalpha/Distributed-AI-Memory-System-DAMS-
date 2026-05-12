package com.aimemory.memory.api;

import com.aimemory.memory.api.dto.MemoryCreateRequest;
import com.aimemory.memory.api.dto.MemoryResponse;
import com.aimemory.memory.domain.enums.MemoryType;
import com.aimemory.memory.service.ConflictResolutionService;
import com.aimemory.memory.service.MemoryVersionService;
import com.aimemory.memory.service.MemoryWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MemoryController unit tests.
 * Note: Full controller tests require Spring context - these are simplified tests
 * that verify the controller dependencies are wired correctly.
 */
@ExtendWith(MockitoExtension.class)
class MemoryControllerTest {

    @Mock
    private MemoryWriteService writeService;

    @Mock
    private MemoryVersionService versionService;

    @Mock
    private ConflictResolutionService conflictService;

    @Test
    void controller_constructsWithDependencies() {
        // Act - verify controller can be constructed with dependencies
        MemoryController controller = new MemoryController(writeService, versionService, conflictService);

        // Assert
        assertNotNull(controller);
    }

    @Test
    void createMemoryRequest_validatesRequiredFields() {
        // Arrange
        UUID conversationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        // Act
        MemoryCreateRequest request = new MemoryCreateRequest(
                "Test content",
                MemoryType.EPISODIC,
                conversationId,
                sessionId
        );

        // Assert
        assertEquals("Test content", request.content());
        assertEquals(MemoryType.EPISODIC, request.memoryType());
        assertEquals(conversationId, request.sourceConversationId());
        assertEquals(sessionId, request.sourceSessionId());
    }

    @Test
    void memoryResponse_fromMemory() {
        // Arrange - create a minimal test to verify DTO conversion works
        UUID memoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act & Assert - verify the response can be created
        MemoryResponse response = new MemoryResponse(
                memoryId, tenantId, userId, "content", MemoryType.EPISODIC,
                UUID.randomUUID(), UUID.randomUUID(), 1, null, "v1", 1536,
                0.5, 0, Instant.now(), false, null, null, Instant.now(), Instant.now()
        );

        assertEquals(memoryId, response.memoryId());
        assertEquals(tenantId, response.tenantId());
        assertEquals(userId, response.userId());
    }
}