package com.aimemory.retrieval.pipeline;

import com.aimemory.shared.util.TokenBudgetCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for allocating context budget to fit within token limits.
 * Trims re-ranked memories to fit within the available token budget.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ContextBudgetAllocator {

    private static final Logger log = LoggerFactory.getLogger(ContextBudgetAllocator.class);

    private final TokenBudgetCalculator tokenCalculator;

    public ContextBudgetAllocator(TokenBudgetCalculator tokenCalculator) {
        this.tokenCalculator = tokenCalculator;
    }

    /**
     * Allocates context budget by trimming re-ranked memories to fit token budget.
     * Uses importance score as a secondary sort to prioritize high-value memories.
     *
     * @param reRanked re-ranked memories from cross-encoder
     * @param tokenBudget maximum tokens available for context
     * @param hardLimit maximum number of memories to return
     * @return trimmed list of memories within budget
     */
    public List<ReRankingService.RankedMemory> allocate(
            List<ReRankingService.RankedMemory> reRanked,
            int tokenBudget,
            int hardLimit) {

        if (reRanked.isEmpty()) {
            return List.of();
        }

        log.debug("Allocating context: tokenBudget={}, hardLimit={}, candidates={}",
                tokenBudget, hardLimit, reRanked.size());

        // Sort by score but also consider importance score
        List<ReRankingService.RankedMemory> sorted = reRanked.stream()
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .toList();

        // Select memories until we hit either token budget or hard limit
        int totalTokens = 0;
        List<ReRankingService.RankedMemory> selected = new java.util.ArrayList<>();

        for (ReRankingService.RankedMemory memory : sorted) {
            int memoryTokens = tokenCalculator.estimateTokens(memory.content());

            if (totalTokens + memoryTokens > tokenBudget || selected.size() >= hardLimit) {
                break;
            }

            selected.add(memory);
            totalTokens += memoryTokens;
        }

        log.info("Context allocation complete: {} memories, {} tokens (budget was {})",
                selected.size(), totalTokens, tokenBudget);

        if (totalTokens < tokenBudget && sorted.size() > selected.size()) {
            log.debug("Unused budget: {} tokens ({} memories not included)",
                    tokenBudget - totalTokens, sorted.size() - selected.size());
        }

        return selected;
    }
}