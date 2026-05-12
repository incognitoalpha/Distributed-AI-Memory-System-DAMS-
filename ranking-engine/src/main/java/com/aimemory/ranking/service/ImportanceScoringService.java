package com.aimemory.ranking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Computes memory importance using a weighted heuristic model.
 * Deliberately avoids LLM calls to keep scoring latency under 5ms.
 * Formula: score = (recencyWeight * recency) + (frequencyWeight * frequency)
 *          + (salienceWeight * salience)
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ImportanceScoringService {

    private static final Logger log = LoggerFactory.getLogger(ImportanceScoringService.class);

    private static final double DEFAULT_RECURSION_WEIGHT = 0.40;
    private static final double DEFAULT_FREQUENCY_WEIGHT = 0.35;
    private static final double DEFAULT_SALIENCE_WEIGHT = 0.25;

    private final RecencyDecayCalculator recencyCalculator;
    private final FrequencyScorer frequencyScorer;
    private final EntitySalienceScorer salienceScorer;

    private final double recencyWeight;
    private final double frequencyWeight;
    private final double salienceWeight;

    public ImportanceScoringService(
            RecencyDecayCalculator recencyCalculator,
            FrequencyScorer frequencyScorer,
            EntitySalienceScorer salienceScorer,

            @Value("${ranking.weights.recency:0.40}") double recencyWeight,
            @Value("${ranking.weights.frequency:0.35}") double frequencyWeight,
            @Value("${ranking.weights.salience:0.25}") double salienceWeight) {

        this.recencyCalculator = recencyCalculator;
        this.frequencyScorer = frequencyScorer;
        this.salienceScorer = salienceScorer;
        this.recencyWeight = recencyWeight;
        this.frequencyWeight = frequencyWeight;
        this.salienceWeight = salienceWeight;
    }

    /**
     * Scores a memory's importance as a double in the range [0.0, 1.0].
     *
     * @param memoryId the memory ID
     * @param createdAt the memory creation timestamp
     * @param retrievalCount number of times retrieved
     * @param content the memory content
     * @return importance score clamped to [0.0, 1.0]
     */
    public double score(UUID memoryId, Instant createdAt, long retrievalCount, String content) {
        double recency = recencyCalculator.computeScore(createdAt);
        double frequency = frequencyScorer.computeScore(retrievalCount);
        double salience = salienceScorer.computeScore(content);

        double raw = (recencyWeight * recency)
                   + (frequencyWeight * frequency)
                   + (salienceWeight * salience);

        double score = Math.max(0.0, Math.min(1.0, raw));

        log.debug("Importance score for memoryId={}: recency={}, frequency={}, salience={}, total={}",
                memoryId, String.format("%.3f", recency), String.format("%.3f", frequency),
                String.format("%.3f", salience), String.format("%.3f", score));

        return score;
    }

    /**
     * Scores a memory with default values for missing data.
     */
    public double scoreWithDefaults(UUID memoryId, Instant createdAt, long retrievalCount, String content) {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (retrievalCount < 0) {
            retrievalCount = 0;
        }
        if (content == null) {
            content = "";
        }

        return score(memoryId, createdAt, retrievalCount, content);
    }
}