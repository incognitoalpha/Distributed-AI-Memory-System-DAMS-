package com.aimemory.retrieval.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for cross-encoder re-ranking of memory candidates.
 * Re-scores initial candidates using a cross-encoder model for better relevance.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ReRankingService {

    private static final Logger log = LoggerFactory.getLogger(ReRankingService.class);

    /**
     * Re-ranks memory candidates using a cross-encoder model.
     * The cross-encoder provides more accurate relevance scoring than the bi-encoder.
     *
     * @param query the original user query
     * @param candidates initial candidates from hybrid search
     * @return re-ranked list of memories
     */
    public List<RankedMemory> reRank(String query, List<HybridSearchService.MemoryCandidate> candidates) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        log.debug("Re-ranking {} candidates for query: {}", candidates.size(),
                query.substring(0, Math.min(50, query.length())));

        // In production, call cross-encoder API (Cohere or local model)
        // For build: use simple scoring based on content similarity

        List<RankedMemory> ranked = candidates.stream()
                .map(candidate -> {
                    double crossEncoderScore = calculateSimpleScore(query, candidate.content());
                    double finalScore = (candidate.score() * 0.3) + (crossEncoderScore * 0.7);

                    return new RankedMemory(
                            candidate.memoryId(),
                            candidate.content(),
                            finalScore,
                            candidate.memoryType(),
                            candidate.sourceConversationId(),
                            candidate.version(),
                            candidate.importanceScore()
                    );
                })
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .toList();

        log.debug("Re-ranking complete, top score: {}", ranked.get(0).score());
        return ranked;
    }

    /**
     * Simple scoring for development/testing.
     * In production, use a cross-encoder model.
     */
    private double calculateSimpleScore(String query, String content) {
        String[] queryTerms = query.toLowerCase().split("\\W+");
        String[] contentTerms = content.toLowerCase().split("\\W+");

        int matches = 0;
        for (String term : queryTerms) {
            if (term.length() > 3) {
                for (String contentTerm : contentTerms) {
                    if (contentTerm.contains(term) || term.contains(contentTerm)) {
                        matches++;
                        break;
                    }
                }
            }
        }

        // Normalize to 0-1 range
        return Math.min(1.0, matches / (double) Math.max(1, queryTerms.length));
    }

    public record RankedMemory(
            UUID memoryId,
            String content,
            double score,
            String memoryType,
            UUID sourceConversationId,
            int version,
            double importanceScore
    ) {}
}