package com.aimemory.pruning.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PruningDecisionServiceTest {

    private PruningDecisionService pruningDecisionService;

    @BeforeEach
    void setUp() {
        pruningDecisionService = new PruningDecisionService(0.15, 90);
    }

    @Test
    void shouldPrune_returnsFalseWhenImportanceHigh() {
        // Arrange
        double highImportance = 0.5;
        Instant oldRetrieval = Instant.now().minusSeconds(100L * 24 * 60 * 60);

        // Act
        boolean result = pruningDecisionService.shouldPrune(highImportance, oldRetrieval);

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldPrune_returnsTrueWhenImportanceLowAndOld() {
        // Arrange
        double lowImportance = 0.1;
        Instant oldRetrieval = Instant.now().minusSeconds(100L * 24 * 60 * 60);

        // Act
        boolean result = pruningDecisionService.shouldPrune(lowImportance, oldRetrieval);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldPrune_returnsTrueWhenImportanceLowAndNeverRetrieved() {
        // Arrange
        double lowImportance = 0.1;
        Instant neverRetrieved = null;

        // Act
        boolean result = pruningDecisionService.shouldPrune(lowImportance, neverRetrieved);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldPrune_returnsFalseWhenRecentRetrieval() {
        // Arrange
        double lowImportance = 0.1;
        Instant recentRetrieval = Instant.now().minusSeconds(10L * 24 * 60 * 60); // 10 days ago

        // Act
        boolean result = pruningDecisionService.shouldPrune(lowImportance, recentRetrieval);

        // Assert - 10 days < 90 day inactivity threshold
        assertFalse(result);
    }

    @Test
    void shouldPrune_boundaryAtThreshold() {
        // Arrange - exactly at threshold should NOT be pruned
        double exactlyAtThreshold = 0.15;
        Instant oldRetrieval = Instant.now().minusSeconds(100L * 24 * 60 * 60);

        // Act
        boolean result = pruningDecisionService.shouldPrune(exactlyAtThreshold, oldRetrieval);

        // Assert
        assertFalse(result);
    }

    @Test
    void findPruningCandidates_returnsEmptyList() {
        // Act - placeholder returns empty list
        UUID tenantId = UUID.randomUUID();
        var result = pruningDecisionService.findPruningCandidates(tenantId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getters_returnConfiguredValues() {
        // Assert
        assertEquals(0.15, pruningDecisionService.getImportanceThreshold());
        assertEquals(90, pruningDecisionService.getInactivityDays());
    }
}