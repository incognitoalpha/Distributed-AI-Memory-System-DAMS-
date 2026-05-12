package com.aimemory.embedding.kafka;

import com.aimemory.embedding.service.EmbeddingGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingRequestConsumerTest {

    @Mock
    private EmbeddingGenerationService generationService;

    @Mock
    private EmbeddingEventPublisher eventPublisher;

    private EmbeddingRequestConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new EmbeddingRequestConsumer(generationService, eventPublisher);
    }

    @Test
    void consume_processesEmbeddingRequest() {
        // Arrange
        String memoryId = UUID.randomUUID().toString();
        String tenantId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String content = "Test memory content";

        EmbeddingRequestConsumer.EmbeddingRequestedEvent event = new EmbeddingRequestConsumer.EmbeddingRequestedEvent(
                memoryId, tenantId, userId, content, UUID.randomUUID().toString(), UUID.randomUUID().toString()
        );

        EmbeddingGenerationService.EmbeddingResult result = new EmbeddingGenerationService.EmbeddingResult(
                UUID.randomUUID(),
                List.of(0.1, 0.2, 0.3),
                "text-embedding-3-small",
                "v1",
                1536,
                true
        );

        when(generationService.generateEmbedding(content)).thenReturn(result);

        // Act
        consumer.consume(event);

        // Assert
        verify(generationService).generateEmbedding(content);
        verify(eventPublisher).publishEmbeddingCompleted(
                eq(memoryId), eq(tenantId), eq(userId), eq(result.vector()),
                eq("text-embedding-3-small"), eq("v1"), eq(1536), eq(true)
        );
    }

    @Test
    void consume_publishesFailureOnException() {
        // Arrange
        String memoryId = UUID.randomUUID().toString();
        String tenantId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String content = "Test memory content";

        EmbeddingRequestConsumer.EmbeddingRequestedEvent event = new EmbeddingRequestConsumer.EmbeddingRequestedEvent(
                memoryId, tenantId, userId, content, UUID.randomUUID().toString(), UUID.randomUUID().toString()
        );

        when(generationService.generateEmbedding(content))
                .thenThrow(new EmbeddingGenerationService.EmbeddingGenerationException("OpenAI error", new RuntimeException()));

        // Act & Assert
        assertThrows(EmbeddingGenerationService.EmbeddingGenerationException.class, () -> {
            consumer.consume(event);
        });

        // Verify that event is not published on success path (exception thrown)
        verify(eventPublisher, never()).publishEmbeddingCompleted(anyString(), anyString(), anyString(), any(), anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    void handleDlt_publishesFailureEvent() {
        // Arrange
        String memoryId = UUID.randomUUID().toString();
        String tenantId = UUID.randomUUID().toString();

        EmbeddingRequestConsumer.EmbeddingRequestedEvent event = new EmbeddingRequestConsumer.EmbeddingRequestedEvent(
                memoryId, tenantId, "user-123", "content", "conv-123", "session-123"
        );

        // Act
        consumer.handleDlt(event);

        // Assert
        verify(eventPublisher).publishEmbeddingFailed(memoryId, tenantId);
    }

    @Test
    void embeddingRequestedEvent_convertsUuids() {
        // Arrange
        UUID memoryUuid = UUID.randomUUID();
        UUID tenantUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        EmbeddingRequestConsumer.EmbeddingRequestedEvent event = new EmbeddingRequestConsumer.EmbeddingRequestedEvent(
                memoryUuid.toString(),
                tenantUuid.toString(),
                userUuid.toString(),
                "content",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        // Act & Assert
        assertEquals(memoryUuid, event.memoryIdAsUuid());
        assertEquals(tenantUuid, event.tenantIdAsUuid());
        assertEquals(userUuid, event.userIdAsUuid());
    }
}