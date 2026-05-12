package com.aimemory.agent.service;

import com.aimemory.agent.client.RetrievalServiceGrpcClient;
import com.aimemory.agent.client.RetrievalServiceGrpcClient.RetrievedMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AgentOrchestrationServiceTest {

    @Mock
    private RetrievalServiceGrpcClient retrievalClient;

    @Mock
    private ContextBuilderService contextBuilder;

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private AgentOrchestrationService orchestrationService;

    @BeforeEach
    void setUp() {
        orchestrationService = new AgentOrchestrationService(
                retrievalClient, contextBuilder, kafkaTemplate
        );

        // Default: Kafka send succeeds (use lenient for tests that don't call this)
        lenient().when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void processMessage_retrievesMemoriesAndBuildsPrompt() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String message = "Hello, what can you tell me?";

        List<RetrievedMemory> memories = List.of(
                new RetrievedMemory(UUID.randomUUID(), "Test memory", 0.9, "EPISODIC", 0.7)
        );

        when(retrievalClient.retrieveMemories(anyString(), any(), any(), anyInt()))
                .thenReturn(memories);
        when(contextBuilder.buildPrompt(anyString(), any())).thenReturn("Test prompt");

        // Act
        AgentOrchestrationService.AgentResponse response = orchestrationService.processMessage(
                message, tenantId, userId, conversationId
        );

        // Assert
        assertNotNull(response);
        assertFalse(response.error());
        assertEquals(1, response.memoriesUsed());
        verify(retrievalClient).retrieveMemories(message, tenantId, userId, 4000);
        verify(contextBuilder).buildPrompt(message, memories);
    }

    @Test
    void processMessage_returnsErrorResponseOnFailure() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String message = "Test message";

        when(retrievalClient.retrieveMemories(anyString(), any(), any(), anyInt()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        AgentOrchestrationService.AgentResponse response = orchestrationService.processMessage(
                message, tenantId, userId, conversationId
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.error());
        assertEquals(0, response.memoriesUsed());
    }

    @Test
    void processMessage_publishesConversationEvent() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String message = "Test message";

        when(retrievalClient.retrieveMemories(anyString(), any(), any(), anyInt()))
                .thenReturn(List.of());
        when(contextBuilder.buildPrompt(anyString(), any())).thenReturn("prompt");

        // Act
        orchestrationService.processMessage(message, tenantId, userId, conversationId);

        // Assert - verify at least one Kafka event is published to conversation topic
        verify(kafkaTemplate, atLeast(1)).send(eq("agent.conversation"), eq(conversationId.toString()), anyMap());
    }

    @Test
    void processMessage_extractsMemoriesFromResponse() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String message = "Test message";

        when(retrievalClient.retrieveMemories(anyString(), any(), any(), anyInt()))
                .thenReturn(List.of());
        when(contextBuilder.buildPrompt(anyString(), any())).thenReturn("prompt");

        // Act
        orchestrationService.processMessage(message, tenantId, userId, conversationId);

        // Assert - verify memory extraction event is published
        verify(kafkaTemplate, atLeast(2)).send(anyString(), anyString(), anyMap());
    }

    @Test
    void agentResponse_record() {
        // Arrange & Act
        AgentOrchestrationService.AgentResponse response = new AgentOrchestrationService.AgentResponse(
                "Test response", 5, false
        );

        // Assert
        assertEquals("Test response", response.response());
        assertEquals(5, response.memoriesUsed());
        assertFalse(response.error());
    }

    @Test
    void agentResponse_errorFlag() {
        // Arrange & Act
        AgentOrchestrationService.AgentResponse response = new AgentOrchestrationService.AgentResponse(
                "Error occurred", 0, true
        );

        // Assert
        assertTrue(response.error());
    }
}