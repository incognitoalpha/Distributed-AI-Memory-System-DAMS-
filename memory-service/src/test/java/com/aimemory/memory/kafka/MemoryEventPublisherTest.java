package com.aimemory.memory.kafka;

import com.aimemory.memory.domain.Memory;
import com.aimemory.memory.domain.enums.MemoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private MemoryEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = new MemoryEventPublisher(kafkaTemplate);
    }

    @Test
    void publishMemoryIngested_sendsCorrectEvent() {
        // Arrange
        Memory memory = createTestMemory();

        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        eventPublisher.publishMemoryIngested(memory);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals("memory.ingested", topicCaptor.getValue());
        assertEquals(memory.getMemoryId().toString(), keyCaptor.getValue());

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals("MEMORY_INGESTED", event.get("eventType"));
        assertEquals(memory.getMemoryId().toString(), event.get("memoryId"));
        assertEquals(memory.getTenantId().toString(), event.get("tenantId"));
        assertEquals(memory.getUserId().toString(), event.get("userId"));
        assertEquals(memory.getContent(), event.get("content"));
        assertEquals(memory.getMemoryType().name(), event.get("memoryType"));
    }

    @Test
    void publishMemoryVersioned_sendsCorrectEvent() {
        // Arrange
        Memory memory = createTestMemory();
        memory.setVersion(2);
        memory.setReplacesMemoryId(UUID.randomUUID());

        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        eventPublisher.publishMemoryVersioned(memory);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), eventCaptor.capture());

        assertEquals("memory.versioned", topicCaptor.getValue());

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals("MEMORY_VERSIONED", event.get("eventType"));
        assertEquals(2, event.get("version"));
        assertNotNull(event.get("replacesMemoryId"));
    }

    @Test
    void publishMemoryVersioned_handlesNullReplacesMemoryId() {
        // Arrange
        Memory memory = createTestMemory();
        memory.setVersion(2);
        memory.setReplacesMemoryId(null);

        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        eventPublisher.publishMemoryVersioned(memory);

        // Assert
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

        Map<String, Object> event = eventCaptor.getValue();
        assertNull(event.get("replacesMemoryId"));
    }

    @Test
    void publishMemoryPruned_sendsCorrectEvent() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        eventPublisher.publishMemoryPruned(memoryId, tenantId, userId);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), eventCaptor.capture());

        assertEquals("memory.pruned", topicCaptor.getValue());

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals("MEMORY_PRUNED", event.get("eventType"));
        assertEquals(memoryId.toString(), event.get("memoryId"));
        assertEquals(tenantId.toString(), event.get("tenantId"));
        assertEquals(userId.toString(), event.get("userId"));
    }

    private Memory createTestMemory() {
        Memory memory = new Memory();
        memory.setMemoryId(UUID.randomUUID());
        memory.setTenantId(UUID.randomUUID());
        memory.setUserId(UUID.randomUUID());
        memory.setContent("Test memory content");
        memory.setMemoryType(MemoryType.EPISODIC);
        memory.setSourceConversationId(UUID.randomUUID());
        memory.setSourceSessionId(UUID.randomUUID());
        memory.setVersion(1);
        memory.setEmbeddingModelVersion("text-embedding-3-small-v1");
        memory.setEmbeddingDimension(1536);
        return memory;
    }
}