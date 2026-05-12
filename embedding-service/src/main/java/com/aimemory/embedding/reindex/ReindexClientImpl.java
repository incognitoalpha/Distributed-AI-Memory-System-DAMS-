package com.aimemory.embedding.reindex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of ReindexClient that updates vector database.
 * In a real scenario, this would call Weaviate/OpenSearch.
 */
@Component
public class ReindexClientImpl implements ReindexBatchJob.ReindexClient {
    private static final Logger log = LoggerFactory.getLogger(ReindexClientImpl.class);

    @Override
    public void updateVector(UUID memoryId, UUID tenantId, List<Double> vector) {
        log.debug("Updating vector for memoryId={} tenantId={}", memoryId, tenantId);
        // Implement actual vector DB update here
    }
}