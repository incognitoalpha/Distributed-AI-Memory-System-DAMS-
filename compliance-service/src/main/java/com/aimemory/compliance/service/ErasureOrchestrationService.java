package com.aimemory.compliance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates GDPR erasure operations.
 * Soft-deletes memories in the database, publishes audit events, 
 * and queues downstream deletion for search and vector stores.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ErasureOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(ErasureOrchestrationService.class);

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final JdbcTemplate jdbcTemplate;

    private final Counter initiationCounter;
    private final Counter completionCounter;
    private final Counter failureCounter;

    public ErasureOrchestrationService(
            KafkaTemplate<String, Map<String, Object>> kafkaTemplate,
            JdbcTemplate jdbcTemplate,
            MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.jdbcTemplate = jdbcTemplate;

        this.initiationCounter = Counter.builder("compliance.erasure.initiated")
                .description("Total number of GDPR erasure requests initiated")
                .register(meterRegistry);
        this.completionCounter = Counter.builder("compliance.erasure.completed")
                .description("Total number of GDPR erasure requests completed")
                .register(meterRegistry);
        this.failureCounter = Counter.builder("compliance.erasure.failed")
                .description("Total number of GDPR erasure requests that failed")
                .register(meterRegistry);
    }

    /**
     * Initiates GDPR erasure for a user.
     * Guaranteed to complete within the 72-hour SLA for initial soft-deletion.
     *
     * @param tenantId the tenant ID
     * @param userId the user ID to erase
     * @param requestId the erasure request ID for tracking
     */
    @Transactional
    public void initiateErasure(UUID tenantId, UUID userId, UUID requestId) {
        log.info("Initiating GDPR erasure: requestId={}, tenantId={}, userId={}",
                requestId, tenantId, userId);
        initiationCounter.increment();

        try {
            // Step 1: Soft-delete all memories for the user in primary DB
            softDeleteUserMemories(tenantId, userId);

            // Step 2: Publish audit event for compliance tracking
            publishAuditEvent(tenantId, userId, "ERASURE_INITIATED", requestId);

            // Step 3: Queue Weaviate vector deletion (async, eventually consistent)
            queueVectorDeletion(tenantId, userId);

            // Step 4: Queue OpenSearch document deletion (async)
            queueSearchDeletion(tenantId, userId);

            log.info("GDPR erasure successfully initiated: requestId={}", requestId);

        } catch (Exception e) {
            log.error("Erasure initiation failed for requestId={}: {}", requestId, e.getMessage(), e);
            failureCounter.increment();
            throw new RuntimeException("GDPR Erasure failed during initiation phase", e);
        }
    }

    /**
     * Completes erasure by updating the tracking request status.
     */
    @Transactional
    public void completeErasure(UUID tenantId, UUID userId, UUID requestId) {
        log.info("Completing GDPR erasure cycle: requestId={}", requestId);

        publishAuditEvent(tenantId, userId, "ERASURE_COMPLETED", requestId);
        updateRequestStatus(requestId, "COMPLETED");
        completionCounter.increment();
    }

    private void softDeleteUserMemories(UUID tenantId, UUID userId) {
        log.debug("Executing soft-delete for userId={} tenantId={}", userId, tenantId);

        String sql = "UPDATE memories SET soft_deleted = true, soft_deleted_at = ? " +
                     "WHERE tenant_id = ? AND user_id = ? AND soft_deleted = false";
        
        int rowsAffected = jdbcTemplate.update(sql, Instant.now(), tenantId, userId);
        
        log.info("Soft-deleted {} memory records for userId={}", rowsAffected, userId);
    }

    private void publishAuditEvent(UUID tenantId, UUID userId, String eventType, UUID requestId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("tenantId", tenantId.toString());
        event.put("userId", userId.toString());
        event.put("requestId", requestId.toString());
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("audit.event", userId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish audit event to Kafka", ex);
                    }
                });
    }

    private void queueVectorDeletion(UUID tenantId, UUID userId) {
        log.debug("Queuing Weaviate vector deletion for userId={}", userId);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "VECTOR_DELETION_REQUESTED");
        event.put("tenantId", tenantId.toString());
        event.put("userId", userId.toString());
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("compliance.vector-deletion", userId.toString(), event);
    }

    private void queueSearchDeletion(UUID tenantId, UUID userId) {
        log.debug("Queuing OpenSearch document deletion for userId={}", userId);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "SEARCH_DELETION_REQUESTED");
        event.put("tenantId", tenantId.toString());
        event.put("userId", userId.toString());
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("compliance.search-deletion", userId.toString(), event);
    }

    private void updateRequestStatus(UUID requestId, String status) {
        log.debug("Updating compliance request status: requestId={}, status={}", requestId, status);
        
        String sql = "UPDATE compliance_requests SET status = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, Instant.now(), requestId);
    }
}
