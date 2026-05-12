package com.aimemory.pruning.scheduler;

import com.aimemory.pruning.service.PruningDecisionService;
import com.aimemory.pruning.service.SoftDeleteService;
import com.aimemory.pruning.service.SummarizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scheduled job that runs daily to prune low-value memories.
 * Runs at 02:00 UTC by default.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class PruningScheduler {

    private static final Logger log = LoggerFactory.getLogger(PruningScheduler.class);

    private final PruningDecisionService decisionService;
    private final SoftDeleteService softDeleteService;
    private final SummarizationService summarizationService;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public PruningScheduler(
            PruningDecisionService decisionService,
            SoftDeleteService softDeleteService,
            SummarizationService summarizationService,
            KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.decisionService = decisionService;
        this.softDeleteService = softDeleteService;
        this.summarizationService = summarizationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Daily pruning job - runs at 02:00 UTC.
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
    public void runPruningJob() {
        log.info("Starting daily pruning job at {}", Instant.now());

        try {
            // Get all tenants (in production, query distinct tenant IDs)
            List<UUID> tenants = getAllTenants();

            int totalPruned = 0;
            int totalSummarized = 0;

            for (UUID tenantId : tenants) {
                // Find pruning candidates
                List<UUID> candidates = decisionService.findPruningCandidates(tenantId);

                if (candidates.isEmpty()) {
                    continue;
                }

                log.info("Processing {} pruning candidates for tenantId={}", candidates.size(), tenantId);

                // Soft-delete each candidate
                int pruned = softDeleteService.softDeleteBatch(candidates);
                totalPruned += pruned;

                // Publish memory.pruned events
                for (UUID memoryId : candidates) {
                    publishPrunedEvent(memoryId, tenantId);
                }

                // Optionally summarize memories before pruning
                // (commented out - would require additional clustering logic)
                // int summarized = summarizeAndReplace(candidates, tenantId);
                // totalSummarized += summarized;
            }

            log.info("Pruning job complete: {} pruned, {} summarized", totalPruned, totalSummarized);

        } catch (Exception e) {
            log.error("Pruning job failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Hard-delete job - runs daily to permanently delete memories
     * whose hard-delete-eligible-at has passed.
     */
    @Scheduled(cron = "0 0 3 * * ?", zone = "UTC")
    public void runHardDeleteJob() {
        log.info("Starting hard-delete job at {}", Instant.now());

        try {
            // In production: query memories where
            // soft_deleted = true AND hard_delete_eligible_at < NOW()

            // DELETE FROM memories WHERE memory_id IN (...)

            log.info("Hard-delete job complete");

        } catch (Exception e) {
            log.error("Hard-delete job failed: {}", e.getMessage(), e);
        }
    }

    private List<UUID> getAllTenants() {
        // In production: query distinct tenant IDs from database
        return List.of();
    }

    private void publishPrunedEvent(UUID memoryId, UUID tenantId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MEMORY_PRUNED");
        event.put("memoryId", memoryId.toString());
        event.put("tenantId", tenantId.toString());
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("memory.pruned", memoryId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish memory.pruned event for memoryId={}", memoryId, ex);
                    }
                });
    }
}