package com.aimemory.retrieval.pipeline;

import com.aimemory.shared.util.TokenBudgetCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ContextBudgetAllocatorTest {

    private ContextBudgetAllocator allocator;

    @BeforeEach
    void setUp() {
        TokenBudgetCalculator calculator = new TokenBudgetCalculator();
        allocator = new ContextBudgetAllocator(calculator);
    }

    @Test
    void allocate_returnsEmptyForEmptyInput() {
        // Act
        List<ReRankingService.RankedMemory> result = allocator.allocate(List.of(), 1000, 10);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void allocate_respectsHardLimit() {
        // Arrange - short contents
        List<ReRankingService.RankedMemory> memories = List.of(
                createMemory("a"),
                createMemory("b"),
                createMemory("c"),
                createMemory("d"),
                createMemory("e")
        );

        // Act - hard limit of 3
        List<ReRankingService.RankedMemory> result = allocator.allocate(memories, 1000, 3);

        // Assert
        assertEquals(3, result.size());
    }

    @Test
    void allocate_respectsTokenBudget() {
        // Arrange - "abc" = 3 chars = 1 token (ceil(3*0.25) = 1)
        // "abcdefghijklmnopqrstuvwxyz" = 26 chars = 7 tokens (ceil)
        List<ReRankingService.RankedMemory> memories = List.of(
                new ReRankingService.RankedMemory(UUID.randomUUID(), "abc", 0.9, "EPISODIC", UUID.randomUUID(), 1, 0.5),
                new ReRankingService.RankedMemory(UUID.randomUUID(), "abc", 0.8, "EPISODIC", UUID.randomUUID(), 1, 0.5),
                new ReRankingService.RankedMemory(UUID.randomUUID(), "abcdefghijklmnopqrstuvwxyz", 0.7, "EPISODIC", UUID.randomUUID(), 1, 0.5)
        );

        // Act - budget for 2 tokens: 2 * "abc" = 2 tokens, adding "abcdef..." = 7 would exceed
        List<ReRankingService.RankedMemory> result = allocator.allocate(memories, 2, 10);

        // Assert - only 2 memories fit in budget (2 tokens)
        assertEquals(2, result.size());
    }

    @Test
    void allocate_sortsByScore() {
        // Arrange
        List<ReRankingService.RankedMemory> memories = List.of(
                new ReRankingService.RankedMemory(UUID.randomUUID(), "c", 0.5, "EPISODIC", UUID.randomUUID(), 1, 0.5),
                new ReRankingService.RankedMemory(UUID.randomUUID(), "b", 0.9, "SEMANTIC", UUID.randomUUID(), 1, 0.7),
                new ReRankingService.RankedMemory(UUID.randomUUID(), "a", 0.7, "EPISODIC", UUID.randomUUID(), 1, 0.4)
        );

        // Act
        List<ReRankingService.RankedMemory> result = allocator.allocate(memories, 1000, 10);

        // Assert - sorted by score descending
        assertEquals(0.9, result.get(0).score(), 0.01);
        assertEquals(0.7, result.get(1).score(), 0.01);
        assertEquals(0.5, result.get(2).score(), 0.01);
    }

    private ReRankingService.RankedMemory createMemory(String content) {
        return new ReRankingService.RankedMemory(
                UUID.randomUUID(),
                content,
                0.9,
                "EPISODIC",
                UUID.randomUUID(),
                1,
                0.5
        );
    }
}