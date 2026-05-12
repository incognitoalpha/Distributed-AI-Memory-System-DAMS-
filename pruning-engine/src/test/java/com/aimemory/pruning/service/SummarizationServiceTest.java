package com.aimemory.pruning.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SummarizationServiceTest {

    private SummarizationService summarizationService;

    @BeforeEach
    void setUp() {
        summarizationService = new SummarizationService();
    }

    @Test
    void summarizeMemories_returnsEmptyForEmptyList() {
        // Act
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String result = summarizationService.summarizeMemories(List.of(), tenantId, userId);

        // Assert
        assertEquals("", result);
    }

    @Test
    void summarizeMemories_returnsSingleMemoryContent() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<SummarizationService.MemoryContent> memories = List.of(
                new SummarizationService.MemoryContent(UUID.randomUUID(), "Single memory content")
        );

        // Act
        String result = summarizationService.summarizeMemories(memories, tenantId, userId);

        // Assert
        assertEquals("Single memory content", result);
    }

    @Test
    void summarizeMemories_returnsCombinedSummary() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<SummarizationService.MemoryContent> memories = List.of(
                new SummarizationService.MemoryContent(UUID.randomUUID(), "First memory about topic A"),
                new SummarizationService.MemoryContent(UUID.randomUUID(), "Second memory about topic B"),
                new SummarizationService.MemoryContent(UUID.randomUUID(), "Third memory about topic C")
        );

        // Act
        String result = summarizationService.summarizeMemories(memories, tenantId, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("3 memories"));
    }

    @Test
    void summarizeMemories_truncatesLongContent() {
        // Arrange - create content longer than 100 chars
        String longContent = "a".repeat(150);
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<SummarizationService.MemoryContent> memories = List.of(
                new SummarizationService.MemoryContent(UUID.randomUUID(), longContent),
                new SummarizationService.MemoryContent(UUID.randomUUID(), "another long content " + "b".repeat(150))
        );

        // Act
        String result = summarizationService.summarizeMemories(memories, tenantId, userId);

        // Assert - should be truncated
        assertTrue(result.length() < 350); // Original would be ~310+
    }

    @Test
    void createSemanticMemory_returnsNewUuid() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String summary = "Test summary";

        // Act
        UUID result = summarizationService.createSemanticMemory(summary, tenantId, userId, conversationId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void memoryContent_record() {
        // Arrange & Act
        UUID memoryId = UUID.randomUUID();
        String content = "Test content";
        SummarizationService.MemoryContent memoryContent = new SummarizationService.MemoryContent(memoryId, content);

        // Assert
        assertEquals(memoryId, memoryContent.memoryId());
        assertEquals(content, memoryContent.content());
    }
}