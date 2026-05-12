package com.aimemory.retrieval.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer for cache invalidation events.
 * Invalidates Redis cache when memories are ingested or versioned.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class CacheInvalidationConsumer {

    private static final Logger log = LoggerFactory.getLogger(CacheInvalidationConsumer.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public CacheInvalidationConsumer(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Handles memory.ingested events - invalidates cache for the user.
     */
    @KafkaListener(topics = "memory.ingested", groupId = "retrieval-service-cache")
    public void handleMemoryIngested(Map<String, Object> event) {
        String tenantId = (String) event.get("tenantId");
        String userId = (String) event.get("userId");

        log.info("Invalidating cache due to memory.ingested: tenantId={}, userId={}", tenantId, userId);
        invalidateCacheForUser(tenantId, userId);
    }

    /**
     * Handles memory.versioned events - invalidates cache for the user.
     */
    @KafkaListener(topics = "memory.versioned", groupId = "retrieval-service-cache")
    public void handleMemoryVersioned(Map<String, Object> event) {
        String tenantId = (String) event.get("tenantId");
        String userId = (String) event.get("userId");

        log.info("Invalidating cache due to memory.versioned: tenantId={}, userId={}", tenantId, userId);
        invalidateCacheForUser(tenantId, userId);
    }

    private void invalidateCacheForUser(String tenantId, String userId) {
        try {
            String pattern = String.format("retrieval:%s:%s:", tenantId, userId);
            // Use scan to find and delete matching keys
            redisTemplate.keys(pattern + "*")
                    .flatMap(redisTemplate::delete)
                    .collectList()
                    .block();
            log.debug("Cache invalidated for tenantId={}, userId={}", tenantId, userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate cache: {}", e.getMessage());
        }
    }
}