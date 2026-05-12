package com.aimemory.embedding.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka publisher for embedding-related events.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class EmbeddingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingEventPublisher.class);

    private static final String TOPIC_EMBEDDING_COMPLETED = "embedding.completed";
    private static final String TOPIC_EMBEDDING_FAILED = "embedding.failed";
    private static final String TOPIC_REINDEX_TRIGGERED = "reindex.triggered";

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public EmbeddingEventPublisher(KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes an embedding.completed event after successful embedding generation.
     */
    public void publishEmbeddingCompleted(
            String memoryId,
            String tenantId,
            String userId,
            List<Double> vector,
            String modelName,
            String modelVersion,
            int dimension,
            boolean success) {

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "EMBEDDING_COMPLETED");
        event.put("memoryId", memoryId);
        event.put("tenantId", tenantId);
        event.put("userId", userId);
        event.put("vector", vector);
        event.put("modelName", modelName);
        event.put("modelVersion", modelVersion);
        event.put("dimension", dimension);
        event.put("success", success);
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send(TOPIC_EMBEDDING_COMPLETED, memoryId, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish embedding.completed for memoryId={}", memoryId, ex);
                    } else {
                        log.debug("Published embedding.completed for memoryId={}", memoryId);
                    }
                });
    }

    /**
     * Publishes an embedding.failed event when embedding permanently fails.
     */
    public void publishEmbeddingFailed(String memoryId, String tenantId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "EMBEDDING_FAILED");
        event.put("memoryId", memoryId);
        event.put("tenantId", tenantId);
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send(TOPIC_EMBEDDING_FAILED, memoryId, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish embedding.failed for memoryId={}", memoryId, ex);
                    }
                });

        log.warn("ALERT: Embedding permanently failed for memoryId={}", memoryId);
    }

    /**
     * Publishes a reindex.triggered event when the active model changes.
     */
    public void publishReindexTriggered(String oldModelVersion, String newModelVersion) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "REINDEX_TRIGGERED");
        event.put("oldModelVersion", oldModelVersion);
        event.put("newModelVersion", newModelVersion);
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send(TOPIC_REINDEX_TRIGGERED, newModelVersion, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish reindex.triggered event", ex);
                    } else {
                        log.info("Published reindex.triggered: {} -> {}",
                                oldModelVersion, newModelVersion);
                    }
                });
    }
}