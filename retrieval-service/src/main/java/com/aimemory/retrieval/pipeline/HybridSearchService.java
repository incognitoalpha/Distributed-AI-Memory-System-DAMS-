package com.aimemory.retrieval.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for hybrid search combining dense vector (Weaviate) and sparse BM25 (OpenSearch).
 * Uses Reciprocal Rank Fusion (RRF) to merge results.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class HybridSearchService {

    private static final Logger log = LoggerFactory.getLogger(HybridSearchService.class);

    private static final int RRF_K = 60; // RRF constant for ranking fusion

    /**
     * Performs parallel hybrid search combining dense and sparse retrieval.
     *
     * @param hydeDocument the HyDE document for dense search
     * @param originalQuery the original query for sparse search
     * @param tenantId the tenant ID for isolation
     * @param userId the user ID
     * @param limit maximum number of candidates to return
     * @return fused list of memory candidates
     */
    public List<MemoryCandidate> search(
            String hydeDocument,
            String originalQuery,
            UUID tenantId,
            UUID userId,
            int limit) {

        log.debug("Starting hybrid search: tenantId={}, userId={}, limit={}", tenantId, userId, limit);

        // Stage 1: Dense vector search (Weaviate)
        List<MemoryCandidate> denseResults = performDenseSearch(hydeDocument, tenantId, userId);
        log.debug("Dense search returned {} results", denseResults.size());

        // Stage 2: Sparse BM25 search (OpenSearch)
        List<MemoryCandidate> sparseResults = performSparseSearch(originalQuery, tenantId, userId);
        log.debug("Sparse search returned {} results", sparseResults.size());

        // Stage 3: Merge using RRF
        List<MemoryCandidate> fused = fuseResults(denseResults, sparseResults, limit);
        log.debug("Hybrid search merged to {} candidates", fused.size());

        return fused;
    }

    /**
     * Dense vector search using Weaviate nearVector.
     */
    private List<MemoryCandidate> performDenseSearch(String hydeDocument, UUID tenantId, UUID userId) {
        // In production: call Weaviate with nearVector search
        // For build: return mock results
        return generateMockCandidates("dense", 20);
    }

    /**
     * Sparse BM25 search using OpenSearch.
     */
    private List<MemoryCandidate> performSparseSearch(String query, UUID tenantId, UUID userId) {
        // In production: call OpenSearch with BM25 query
        // For build: return mock results
        return generateMockCandidates("sparse", 20);
    }

    /**
     * Reciprocal Rank Fusion to merge dense and sparse results.
     */
    private List<MemoryCandidate> fuseResults(List<MemoryCandidate> dense, List<MemoryCandidate> sparse, int limit) {
        Map<UUID, Double> scores = new HashMap<>();

        // Score dense results
        for (int i = 0; i < dense.size(); i++) {
            double rrfScore = 1.0 / (RRF_K + i + 1);
            MemoryCandidate candidate = dense.get(i);
            scores.merge(candidate.memoryId(), rrfScore, Double::sum);
        }

        // Score sparse results
        for (int i = 0; i < sparse.size(); i++) {
            double rrfScore = 1.0 / (RRF_K + i + 1);
            MemoryCandidate candidate = sparse.get(i);
            scores.merge(candidate.memoryId(), rrfScore, Double::sum);
        }

        // Sort by fused score and return top results
        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(entry -> {
                    // Find the original candidate (prefer dense result if exists)
                    return dense.stream()
                            .filter(c -> c.memoryId().equals(entry.getKey()))
                            .findFirst()
                            .orElseGet(() -> sparse.stream()
                                    .filter(c -> c.memoryId().equals(entry.getKey()))
                                    .findFirst()
                                    .orElse(null));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<MemoryCandidate> generateMockCandidates(String source, int count) {
        List<MemoryCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            candidates.add(new MemoryCandidate(
                    UUID.randomUUID(),
                    "Memory content " + i + " from " + source + " search with relevant information about the query.",
                    1.0 - (i * 0.03), // Decreasing score
                    "EPISODIC",
                    UUID.randomUUID(),
                    1,
                    0.5
            ));
        }
        return candidates;
    }

    public record MemoryCandidate(
            UUID memoryId,
            String content,
            double score,
            String memoryType,
            UUID sourceConversationId,
            int version,
            double importanceScore
    ) {}
}