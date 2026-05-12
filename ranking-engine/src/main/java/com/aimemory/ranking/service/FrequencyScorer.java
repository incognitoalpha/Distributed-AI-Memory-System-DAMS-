package com.aimemory.ranking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Scores memory importance based on retrieval frequency.
 * Uses logarithmic scale for diminishing returns.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class FrequencyScorer {

    private static final double MAX_RETRIEVALS = 20.0;

    private final double retrievalCap;

    public FrequencyScorer(
            @Value("${ranking.frequency.cap:20}") double retrievalCap) {
        this.retrievalCap = retrievalCap;
    }

    /**
     * Computes frequency score using logarithmic scale.
     * Returns 1.0 at retrievalCap, with diminishing returns beyond.
     *
     * @param retrievalCount number of times the memory has been retrieved
     * @return score between 0.0 and 1.0
     */
    public double computeScore(long retrievalCount) {
        if (retrievalCount <= 0) {
            return 0.0;
        }
        return Math.min(1.0, Math.log1p(retrievalCount) / Math.log1p(retrievalCap));
    }

    public double getRetrievalCap() {
        return retrievalCap;
    }
}