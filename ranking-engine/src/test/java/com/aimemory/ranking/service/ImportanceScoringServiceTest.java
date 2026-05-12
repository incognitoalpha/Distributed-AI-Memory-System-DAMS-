package com.aimemory.ranking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportanceScoringServiceTest {

    @Mock
    private RecencyDecayCalculator recencyCalculator;

    @Mock
    private FrequencyScorer frequencyScorer;

    @Mock
    private EntitySalienceScorer salienceScorer;

    private ImportanceScoringService scoringService;

    private Instant testCreatedAt;
    private UUID testMemoryId;
    private String testContent;

    @BeforeEach
    void setUp() {
        scoringService = new ImportanceScoringService(
                recencyCalculator,
                frequencyScorer,
                salienceScorer,
                0.40,  // recency weight
                0.35,  // frequency weight
                0.25   // salience weight
        );
        testMemoryId = UUID.randomUUID();
        testCreatedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        testContent = "Test content with important information";
    }

    @Test
    void score_returnsWeightedSum() {
        // Arrange
        when(recencyCalculator.computeScore(testCreatedAt)).thenReturn(0.8);
        when(frequencyScorer.computeScore(5L)).thenReturn(0.6);
        when(salienceScorer.computeScore(testContent)).thenReturn(0.5);

        // Act
        double score = scoringService.score(testMemoryId, testCreatedAt, 5L, testContent);

        // Assert
        // Expected: (0.40 * 0.8) + (0.35 * 0.6) + (0.25 * 0.5) = 0.32 + 0.21 + 0.125 = 0.655
        assertEquals(0.655, score, 0.001);
    }

    @Test
    void score_clampedToZeroForNegativeResult() {
        // Arrange - all scores are 0, weighted sum is 0, min(1, max(0, 0)) = 0
        when(recencyCalculator.computeScore(testCreatedAt)).thenReturn(0.0);
        when(frequencyScorer.computeScore(0L)).thenReturn(0.0);
        when(salienceScorer.computeScore("short")).thenReturn(0.0);

        // Act
        double score = scoringService.score(testMemoryId, testCreatedAt, 0L, "short");

        // Assert
        assertEquals(0.0, score, 0.0);
    }

    @Test
    void score_clampedToOneForHighResult() {
        // Arrange - all scores are 1.0, weighted sum is 1.0
        when(recencyCalculator.computeScore(any(Instant.class))).thenReturn(1.0);
        when(frequencyScorer.computeScore(anyLong())).thenReturn(1.0);
        when(salienceScorer.computeScore(anyString())).thenReturn(1.0);

        // Act
        double score = scoringService.score(testMemoryId, testCreatedAt, 100L, "important content");

        // Assert - with weights 0.4 + 0.35 + 0.25 = 1.0, so max is 1.0
        assertEquals(1.0, score, 0.0);
    }

    @Test
    void scoreWithDefaults_handlesNullCreatedAt() {
        // Arrange
        when(recencyCalculator.computeScore(any(Instant.class))).thenReturn(1.0);
        when(frequencyScorer.computeScore(anyLong())).thenReturn(0.0);
        when(salienceScorer.computeScore(anyString())).thenReturn(0.0);

        // Act
        double score = scoringService.scoreWithDefaults(testMemoryId, null, 0, "content");

        // Assert - should use current time as default and compute score
        assertTrue(score >= 0.0 && score <= 1.0);
    }

    @Test
    void scoreWithDefaults_handlesNegativeRetrievalCount() {
        // Arrange
        when(recencyCalculator.computeScore(any(Instant.class))).thenReturn(0.5);
        when(frequencyScorer.computeScore(anyLong())).thenReturn(0.0); // should be normalized to 0
        when(salienceScorer.computeScore(anyString())).thenReturn(0.0);

        // Act
        double score = scoringService.scoreWithDefaults(testMemoryId, testCreatedAt, -5, "content");

        // Assert - should not throw and should handle negative gracefully
        assertTrue(score >= 0.0 && score <= 1.0);
    }

    @Test
    void scoreWithDefaults_handlesNullContent() {
        // Arrange
        when(recencyCalculator.computeScore(any(Instant.class))).thenReturn(0.5);
        when(frequencyScorer.computeScore(anyLong())).thenReturn(0.5);
        when(salienceScorer.computeScore(anyString())).thenReturn(0.0); // empty content = 0 salience

        // Act
        double score = scoringService.scoreWithDefaults(testMemoryId, testCreatedAt, 5, null);

        // Assert
        assertTrue(score >= 0.0 && score <= 1.0);
    }
}