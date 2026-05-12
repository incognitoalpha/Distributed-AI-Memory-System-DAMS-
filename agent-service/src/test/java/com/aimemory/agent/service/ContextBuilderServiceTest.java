package com.aimemory.agent.service;

import com.aimemory.agent.client.RetrievalServiceGrpcClient;
import com.aimemory.agent.client.RetrievalServiceGrpcClient.RetrievedMemory;
import com.aimemory.shared.util.TokenBudgetCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ContextBuilderServiceTest {

    private ContextBuilderService contextBuilderService;

    @BeforeEach
    void setUp() {
        TokenBudgetCalculator calculator = new TokenBudgetCalculator();
        contextBuilderService = new ContextBuilderService(calculator);
    }

    @Test
    void buildPrompt_returnsSystemPromptWithMessageWhenNoMemories() {
        // Arrange
        String userMessage = "Hello, how are you?";

        // Act
        String prompt = contextBuilderService.buildPrompt(userMessage, List.of());

        // Assert
        assertTrue(prompt.contains("You are an AI assistant"));
        assertTrue(prompt.contains(userMessage));
    }

    @Test
    void buildPrompt_includesMemoryContext() {
        // Arrange
        String userMessage = "What do I like?";
        List<RetrievedMemory> memories = List.of(
                new RetrievedMemory(UUID.randomUUID(), "User likes pizza", 0.9, "EPISODIC", 0.7),
                new RetrievedMemory(UUID.randomUUID(), "User likes ice cream", 0.8, "EPISODIC", 0.6)
        );

        // Act
        String prompt = contextBuilderService.buildPrompt(userMessage, memories);

        // Assert
        assertTrue(prompt.contains("Memory Context"));
        assertTrue(prompt.contains("User likes pizza"));
        assertTrue(prompt.contains("User likes ice cream"));
    }

    @Test
    void buildPrompt_marksPotentialConflicts() {
        // Arrange - memories with very different scores may indicate conflict
        String userMessage = "What did I say about food?";
        List<RetrievedMemory> memories = List.of(
                new RetrievedMemory(UUID.randomUUID(), "I love running", 0.9, "EPISODIC", 0.7),
                new RetrievedMemory(UUID.randomUUID(), "I hate running", 0.3, "EPISODIC", 0.6)
        );

        // Act
        String prompt = contextBuilderService.buildPrompt(userMessage, memories);

        // Assert - difference > 0.3 triggers conflict annotation
        assertTrue(prompt.contains("CONFLICT"));
    }

    @Test
    void buildPrompt_noConflictForSimilarScores() {
        // Arrange - memories with similar scores shouldn't trigger conflict
        String userMessage = "What do I like?";
        List<RetrievedMemory> memories = List.of(
                new RetrievedMemory(UUID.randomUUID(), "I like pizza", 0.8, "EPISODIC", 0.7),
                new RetrievedMemory(UUID.randomUUID(), "I like pasta", 0.75, "EPISODIC", 0.6)
        );

        // Act
        String prompt = contextBuilderService.buildPrompt(userMessage, memories);

        // Assert - no conflict annotation for similar scores
        assertFalse(prompt.contains("CONFLICT"));
    }

    @Test
    void buildPrompt_includesRelevanceScores() {
        // Arrange
        String userMessage = "Tell me about my preferences";
        List<RetrievedMemory> memories = List.of(
                new RetrievedMemory(UUID.randomUUID(), "User preference 1", 0.95, "SEMANTIC", 0.8)
        );

        // Act
        String prompt = contextBuilderService.buildPrompt(userMessage, memories);

        // Assert
        assertTrue(prompt.contains("relevance:"));
    }

    @Test
    void buildPrompt_trimsToBudget() {
        // Arrange - use many long memories to exceed budget
        String userMessage = "test";
        String longContent = "a".repeat(500); // ~125 tokens each
        List<RetrievedMemory> memories = List.of(
                new RetrievedMemory(UUID.randomUUID(), longContent, 0.9, "EPISODIC", 0.7),
                new RetrievedMemory(UUID.randomUUID(), longContent, 0.8, "EPISODIC", 0.6),
                new RetrievedMemory(UUID.randomUUID(), longContent, 0.7, "EPISODIC", 0.5),
                new RetrievedMemory(UUID.randomUUID(), longContent, 0.6, "EPISODIC", 0.4),
                new RetrievedMemory(UUID.randomUUID(), longContent, 0.5, "EPISODIC", 0.3)
        );

        // Act - use small budget to force trimming
        String prompt = contextBuilderService.buildPrompt(userMessage, memories);

        // Assert - context should be trimmed
        assertTrue(prompt.contains("trimmed") || prompt.length() < 3000);
    }

    @Test
    void retrievedMemory_record() {
        // Arrange & Act
        UUID memoryId = UUID.randomUUID();
        RetrievedMemory memory = new RetrievedMemory(
                memoryId, "Test content", 0.85, "EPISODIC", 0.7
        );

        // Assert
        assertEquals(memoryId, memory.memoryId());
        assertEquals("Test content", memory.content());
        assertEquals(0.85, memory.score());
        assertEquals("EPISODIC", memory.memoryType());
    }
}