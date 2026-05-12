package com.aimemory.retrieval.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReRankingServiceTest {

    private ReRankingService reRankingService;

    @BeforeEach
    void setUp() {
        reRankingService = new ReRankingService();
    }

    @Test
    void reRank_returnsEmptyForEmptyCandidates() {
        // Act
        List<ReRankingService.RankedMemory> result = reRankingService.reRank("query", List.of());

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void reRank_returnsRankedResults() {
        // Arrange
        List<HybridSearchService.MemoryCandidate> candidates = List.of(
                new HybridSearchService.MemoryCandidate(
                        UUID.randomUUID(), "content 1", 0.8, "EPISODIC", UUID.randomUUID(), 1, 0.5
                ),
                new HybridSearchService.MemoryCandidate(
                        UUID.randomUUID(), "content 2", 0.9, "SEMANTIC", UUID.randomUUID(), 1, 0.7
                )
        );

        // Act
        List<ReRankingService.RankedMemory> result = reRankingService.reRank("test query", candidates);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).score() >= result.get(1).score());
    }

    @Test
    void reRank_preservesAllCandidateData() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        HybridSearchService.MemoryCandidate candidate = new HybridSearchService.MemoryCandidate(
                memoryId, "test content", 0.8, "EPISODIC", conversationId, 2, 0.6
        );

        // Act
        List<ReRankingService.RankedMemory> result = reRankingService.reRank("test", List.of(candidate));

        // Assert
        assertEquals(1, result.size());
        assertEquals(memoryId, result.get(0).memoryId());
        assertEquals("test content", result.get(0).content());
        assertEquals("EPISODIC", result.get(0).memoryType());
        assertEquals(conversationId, result.get(0).sourceConversationId());
        assertEquals(2, result.get(0).version());
        assertEquals(0.6, result.get(0).importanceScore());
    }

    @Test
    void reRank_sortsByScoreDescending() {
        // Arrange - use very different scores to ensure proper sorting
        List<HybridSearchService.MemoryCandidate> candidates = List.of(
                new HybridSearchService.MemoryCandidate(
                        UUID.randomUUID(), "word1 word2 word3", 0.1, "EPISODIC", UUID.randomUUID(), 1, 0.5
                ),
                new HybridSearchService.MemoryCandidate(
                        UUID.randomUUID(), "test query word1 word2", 0.95, "SEMANTIC", UUID.randomUUID(), 1, 0.7
                ),
                new HybridSearchService.MemoryCandidate(
                        UUID.randomUUID(), "test word1 word2", 0.5, "EPISODIC", UUID.randomUUID(), 1, 0.4
                )
        );

        // Act
        List<ReRankingService.RankedMemory> result = reRankingService.reRank("test query", candidates);

        // Assert - sorted by score descending (highest first)
        assertTrue(result.get(0).score() >= result.get(1).score());
        assertTrue(result.get(1).score() >= result.get(2).score());
    }

    @Test
    void rankedMemory_record() {
        // Arrange & Act
        ReRankingService.RankedMemory ranked = new ReRankingService.RankedMemory(
                UUID.randomUUID(),
                "Ranked content",
                0.85,
                "SEMANTIC",
                UUID.randomUUID(),
                3,
                0.9
        );

        // Assert
        assertEquals(0.85, ranked.score());
        assertEquals("SEMANTIC", ranked.memoryType());
        assertEquals(3, ranked.version());
    }
}