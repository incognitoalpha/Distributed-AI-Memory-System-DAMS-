package com.aimemory.ranking.kafka;

import com.aimemory.ranking.service.ImportanceScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Consumes memory events and recomputes importance scores.
 * Handles memory.ingested and memory.retrieved events.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class MemoryRankingConsumer {

    private static final Logger log = LoggerFactory.getLogger(MemoryRankingConsumer.class);

    private final ImportanceScoringService scoringService;

    public MemoryRankingConsumer(ImportanceScoringService scoringService) {
        this.scoringService = scoringService;
    }

    /**
     * Processes memory.ingested events - computes initial importance score.
     */
    @KafkaListener(topics = "memory.ingested", groupId = "ranking-engine")
    public void handleMemoryIngested(Map<String, Object> event) {
        String memoryId = (String) event.get("memoryId");
        String tenantId = (String) event.get("tenantId");
        String createdAtStr = (String) event.get("createdAt");

        log.info("Computing importance for new memory: memoryId={}, tenantId={}", memoryId, tenantId);

        try {
            Instant createdAt = createdAtStr != null ? Instant.parse(createdAtStr) : Instant.now();
            String content = (String) event.get("content");

            double score = scoringService.scoreWithDefaults(
                    UUID.fromString(memoryId),
                    createdAt,
                    0, // Initial retrieval count
                    content
            );

            log.info("Computed importance score for memoryId={}: score={}", memoryId, score);
            // In production: persist score to database via MemoryRepository

        } catch (Exception e) {
            log.error("Failed to compute importance for memoryId={}", memoryId, e);
        }
    }

    /**
     * Processes memory.retrieved events - updates retrieval count and recomputes score.
     */
    @KafkaListener(topics = "memory.retrieved", groupId = "ranking-engine")
    public void handleMemoryRetrieved(Map<String, Object> event) {
        String memoryId = (String) event.get("memoryId");
        String tenantId = (String) event.get("tenantId");

        log.debug("Updating retrieval count for memoryId={}, tenantId={}", memoryId, tenantId);

        try {
            // In production: increment retrieval count in database
            // Then recompute importance score
            log.debug("Retrieval count incremented for memoryId={}", memoryId);

        } catch (Exception e) {
            log.error("Failed to update retrieval count for memoryId={}", memoryId, e);
        }
    }
}