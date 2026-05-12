package com.aimemory.retrieval.pipeline;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates the full retrieval pipeline for a user query.
 * Pipeline: HyDE rewrite → parallel hybrid search → cross-encoder re-rank → budget allocation.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class RetrievalPipelineService {

    private static final Logger log = LoggerFactory.getLogger(RetrievalPipelineService.class);

    private static final int INITIAL_CANDIDATE_LIMIT = 50;
    private static final int FINAL_CONTEXT_LIMIT = 10;

    private final QueryRewriterService queryRewriter;
    private final HybridSearchService hybridSearch;
    private final ReRankingService reRanker;
    private final ContextBudgetAllocator budgetAllocator;

    private final Timer pipelineTimer;
    private final Counter hitRateCounter;
    private final Counter missRateCounter;

    public RetrievalPipelineService(
            QueryRewriterService queryRewriter,
            HybridSearchService hybridSearch,
            ReRankingService reRanker,
            ContextBudgetAllocator budgetAllocator,
            MeterRegistry meterRegistry) {
        this.queryRewriter = queryRewriter;
        this.hybridSearch = hybridSearch;
        this.reRanker = reRanker;
        this.budgetAllocator = budgetAllocator;

        // Metrics
        this.pipelineTimer = Timer.builder("memory.retrieval.pipeline.duration")
                .description("Time taken for full retrieval pipeline")
                .register(meterRegistry);

        this.hitRateCounter = Counter.builder("memory.retrieval.hit_rate")
                .description("Count of retrievals with results")
                .register(meterRegistry);

        this.missRateCounter = Counter.builder("memory.retrieval.miss_rate")
                .description("Count of retrievals with no results")
                .register(meterRegistry);
    }

    /**
     * Executes the full retrieval pipeline for a given query within a tenant context.
     *
     * @param query the raw user query string
     * @param tenantId the tenant identifier
     * @param userId the user identifier
     * @param tokenBudget maximum number of tokens available for context injection
     * @return ordered list of memories to inject into the LLM prompt
     */
    public List<ReRankingService.RankedMemory> retrieve(
            String query, UUID tenantId, UUID userId, int tokenBudget) {

        long startTime = System.nanoTime();

        try {
            log.info("Starting retrieval pipeline: tenantId={}, userId={}, query={}",
                    tenantId, userId, query.substring(0, Math.min(50, query.length())));

            // Stage 1: HyDE — generate a hypothetical document
            String hydeDocument = queryRewriter.generateHypotheticalDocument(query);
            log.debug("Stage 1 (HyDE) complete: document length={}", hydeDocument.length());

            // Stage 2: Parallel hybrid search
            List<HybridSearchService.MemoryCandidate> candidates = hybridSearch.search(
                    hydeDocument, query, tenantId, userId, INITIAL_CANDIDATE_LIMIT);

            if (candidates.isEmpty()) {
                log.info("No candidates found for tenantId={} userId={}", tenantId, userId);
                missRateCounter.increment();
                return List.of();
            }

            log.debug("Stage 2 (Hybrid Search) complete: {} candidates", candidates.size());

            // Stage 3: Cross-encoder re-ranking
            List<ReRankingService.RankedMemory> reRanked = reRanker.reRank(query, candidates);
            log.debug("Stage 3 (Re-ranking) complete: top score={}", reRanked.get(0).score());

            // Stage 4: Token budget allocation
            List<ReRankingService.RankedMemory> result = budgetAllocator.allocate(
                    reRanked, tokenBudget, FINAL_CONTEXT_LIMIT);

            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            pipelineTimer.record(duration, TimeUnit.MILLISECONDS);

            if (result.isEmpty()) {
                missRateCounter.increment();
            } else {
                hitRateCounter.increment();
            }

            log.info("Retrieval complete: {} memories returned in {}ms", result.size(), duration);
            return result;

        } catch (Exception e) {
            log.error("Retrieval pipeline failed: {}", e.getMessage(), e);
            missRateCounter.increment();
            throw new RetrievalException("Pipeline failed: " + e.getMessage(), e);
        }
    }

    public static class RetrievalException extends RuntimeException {
        public RetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}