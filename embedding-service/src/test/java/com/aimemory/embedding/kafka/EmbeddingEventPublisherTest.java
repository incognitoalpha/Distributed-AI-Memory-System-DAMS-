package com.aimemory.embedding.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private EmbeddingEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = new EmbeddingEventPublisher(kafkaTemplate);
    }

    @Test
    void publishEmbeddingCompleted_sendsCorrectEvent() {
        // Arrange
        String memoryId = "memory-123";
        String tenantId = "tenant-456";
        String userId = "user-789";
        List<Double> vector = List.of(0.1, 0.2, 0.3);

        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        eventPublisher.publishEmbeddingCompleted(
                memoryId, tenantId, userId, vector, "text-embedding-3-small", "v1", 1536, true
        );

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals("embedding.completed", topicCaptor.getValue());
        assertEquals(memoryId, keyCaptor.getValue());

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals("EMBEDDING_COMPLETED", event.get("eventType"));
        assertEquals(memoryId, event.get("memoryId"));
        assertEquals(tenantId, event.get("tenantId"));
        assertEquals("text-embedding-3-small", event.get("modelName"));
        assertEquals("v1", event.get("modelVersion"));
        assertEquals(1536, event.get("dimension"));
        assertEquals(true, event.get("success"));
    }

    @Test
    void publishEmbeddingFailed_sendsFailureEvent() {
        // Arrange
        String memoryId = "memory-123";
        String tenantId = "tenant-456";

        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        eventPublisher.publishEmbeddingFailed(memoryId, tenantId);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), eventCaptor.capture());

        assertEquals("embedding.failed", topicCaptor.getValue());

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals("EMBEDDING_FAILED", event.get("eventType"));
        assertEquals(memoryId, event.get("memoryId"));
        assertEquals(tenantId, event.get("tenantId"));
    }

    @Test
    void publishReindexTriggered_sendsReindexEvent() {
        // Arrange
        String oldVersion = "v1";
        String newVersion = "v2";

        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        eventPublisher.publishReindexTriggered(oldVersion, newVersion);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), eventCaptor.capture());

        assertEquals("reindex.triggered", topicCaptor.getValue());

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals("REINDEX_TRIGGERED", event.get("eventType"));
        assertEquals(oldVersion, event.get("oldModelVersion"));
        assertEquals(newVersion, event.get("newModelVersion"));
    }
}