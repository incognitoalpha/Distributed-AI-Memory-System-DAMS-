package com.aimemory.retrieval.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HybridSearchServiceTest {

    private HybridSearchService hybridSearchService;

    @BeforeEach
    void setUp() {
        hybridSearchService = new HybridSearchService();
    }

    @Test
    void search_returnsCandidates() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        List<HybridSearchService.MemoryCandidate> results = hybridSearchService.search(
                "hypothetical document", "original query", tenantId, userId, 10
        );

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.size() <= 10);
    }

    @Test
    void search_returnsUniqueCandidates() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        List<HybridSearchService.MemoryCandidate> results = hybridSearchService.search(
                "hypothetical document", "original query", tenantId, userId, 50
        );

        // Assert - no duplicate memory IDs
        List<UUID> memoryIds = results.stream().map(HybridSearchService.MemoryCandidate::memoryId).toList();
        assertEquals(memoryIds.size(), memoryIds.stream().distinct().count());
    }

    @Test
    void search_respectsLimit() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        List<HybridSearchService.MemoryCandidate> results = hybridSearchService.search(
                "hypothetical document", "original query", tenantId, userId, 5
        );

        // Assert
        assertTrue(results.size() <= 5);
    }

    @Test
    void memoryCandidate_record() {
        // Arrange & Act
        HybridSearchService.MemoryCandidate candidate = new HybridSearchService.MemoryCandidate(
                UUID.randomUUID(),
                "Test content",
                0.95,
                "EPISODIC",
                UUID.randomUUID(),
                1,
                0.8
        );

        // Assert
        assertEquals("Test content", candidate.content());
        assertEquals(0.95, candidate.score());
        assertEquals("EPISODIC", candidate.memoryType());
    }
}