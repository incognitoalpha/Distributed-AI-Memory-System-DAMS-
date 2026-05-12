package com.aimemory.ranking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Calculates memory importance decay based on recency.
 * Uses exponential decay with configurable half-life.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class RecencyDecayCalculator {

    private final double halfLifeDays;

    public RecencyDecayCalculator(
            @Value("${ranking.decay.half-life-days:30}") double halfLifeDays) {
        this.halfLifeDays = halfLifeDays;
    }

    /**
     * Computes recency score using exponential decay.
     * Score halves every halfLifeDays days.
     *
     * @param createdAt the memory creation timestamp
     * @return score between 0.0 and 1.0
     */
    public double computeScore(Instant createdAt) {
        long ageInDays = ChronoUnit.DAYS.between(createdAt, Instant.now());
        return Math.pow(0.5, ageInDays / halfLifeDays);
    }

    public double getHalfLifeDays() {
        return halfLifeDays;
    }
}