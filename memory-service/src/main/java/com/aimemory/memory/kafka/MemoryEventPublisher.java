package com.aimemory.memory.kafka;

import com.aimemory.memory.domain.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka publisher for memory events.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class MemoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MemoryEventPublisher.class);

    private static final String TOPIC_MEMORY_INGESTED = "memory.ingested";
    private static final String TOPIC_MEMORY_VERSIONED = "memory.versioned";

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public MemoryEventPublisher(KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a memory.ingested event when a new memory is created.
     *
     * @param memory the created memory
     */
    public void publishMemoryIngested(Memory memory) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MEMORY_INGESTED");
        event.put("memoryId", memory.getMemoryId().toString());
        event.put("tenantId", memory.getTenantId().toString());
        event.put("userId", memory.getUserId().toString());
        event.put("content", memory.getContent());
        event.put("memoryType", memory.getMemoryType().name());
        event.put("sourceConversationId", memory.getSourceConversationId().toString());
        event.put("sourceSessionId", memory.getSourceSessionId().toString());
        event.put("embeddingModelVersion", memory.getEmbeddingModelVersion());
        event.put("embeddingDimension", memory.getEmbeddingDimension());
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send(TOPIC_MEMORY_INGESTED, memory.getMemoryId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish memory.ingested event for memoryId={}",
                                memory.getMemoryId(), ex);
                    } else {
                        log.debug("Published memory.ingested event for memoryId={}",
                                memory.getMemoryId());
                    }
                });
    }

    /**
     * Publishes a memory.versioned event when a memory is updated.
     *
     * @param memory the updated memory
     */
    public void publishMemoryVersioned(Memory memory) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MEMORY_VERSIONED");
        event.put("memoryId", memory.getMemoryId().toString());
        event.put("tenantId", memory.getTenantId().toString());
        event.put("userId", memory.getUserId().toString());
        event.put("content", memory.getContent());
        event.put("version", memory.getVersion());
        event.put("replacesMemoryId", memory.getReplacesMemoryId() != null
                ? memory.getReplacesMemoryId().toString() : null);
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send(TOPIC_MEMORY_VERSIONED, memory.getMemoryId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish memory.versioned event for memoryId={}",
                                memory.getMemoryId(), ex);
                    } else {
                        log.debug("Published memory.versioned event for memoryId={}",
                                memory.getMemoryId());
                    }
                });
    }

    /**
     * Publishes a memory.pruned event when a memory is soft-deleted by the pruning engine.
     *
     * @param memoryId the pruned memory ID
     * @param tenantId the tenant ID
     * @param userId the user ID
     */
    public void publishMemoryPruned(UUID memoryId, UUID tenantId, UUID userId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MEMORY_PRUNED");
        event.put("memoryId", memoryId.toString());
        event.put("tenantId", tenantId.toString());
        event.put("userId", userId.toString());
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("memory.pruned", memoryId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish memory.pruned event for memoryId={}", memoryId, ex);
                    } else {
                        log.debug("Published memory.pruned event for memoryId={}", memoryId);
                    }
                });
    }
}