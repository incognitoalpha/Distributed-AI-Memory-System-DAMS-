package com.aimemory.pruning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Determines which memories are candidates for pruning.
 * Selects memories with low importance score and no recent retrieval.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class PruningDecisionService {

    private static final Logger log = LoggerFactory.getLogger(PruningDecisionService.class);

    private final double importanceThreshold;
    private final int inactivityDays;

    public PruningDecisionService(
            @Value("${pruning.importance-threshold:0.15}") double importanceThreshold,
            @Value("${pruning.inactivity-days:90}") int inactivityDays) {
        this.importanceThreshold = importanceThreshold;
        this.inactivityDays = inactivityDays;
    }

    /**
     * Identifies memories eligible for pruning.
     *
     * @param tenantId the tenant ID
     * @return list of memory IDs eligible for pruning
     */
    public List<UUID> findPruningCandidates(UUID tenantId) {
        log.info("Finding pruning candidates for tenantId={}, threshold={}, inactivity={} days",
                tenantId, importanceThreshold, inactivityDays);

        Instant cutoffDate = Instant.now().minusSeconds((long) inactivityDays * 24 * 60 * 60);

        // In production: query database for memories meeting criteria
        // SELECT memory_id FROM memories
        // WHERE tenant_id = :tenantId
        // AND importance_score < :threshold
        // AND (last_retrieved_at IS NULL OR last_retrieved_at < :cutoffDate)
        // AND soft_deleted = false

        List<UUID> candidates = List.of(); // Placeholder
        log.info("Found {} pruning candidates for tenantId={}", candidates.size(), tenantId);

        return candidates;
    }

    /**
     * Checks if a memory should be pruned based on criteria.
     */
    public boolean shouldPrune(double importanceScore, Instant lastRetrievedAt) {
        // Check importance threshold
        if (importanceScore >= importanceThreshold) {
            return false;
        }

        // Check inactivity
        if (lastRetrievedAt != null) {
            long daysSinceRetrieval = (Instant.now().getEpochSecond() - lastRetrievedAt.getEpochSecond()) / (24 * 60 * 60);
            if (daysSinceRetrieval < inactivityDays) {
                return false;
            }
        }

        return true;
    }

    public double getImportanceThreshold() {
        return importanceThreshold;
    }

    public int getInactivityDays() {
        return inactivityDays;
    }
}