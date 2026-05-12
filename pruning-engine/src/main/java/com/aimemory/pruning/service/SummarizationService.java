package com.aimemory.pruning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Summarizes clusters of episodic memories into semantic memories.
 * Uses LLM (LangChain4j) to compress multiple memories into one.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class SummarizationService {

    private static final Logger log = LoggerFactory.getLogger(SummarizationService.class);

    /**
     * Summarizes a cluster of episodic memories into a single semantic memory.
     * The semantic memory captures the essential information from the cluster.
     *
     * @param memories list of episodic memories to summarize
     * @param tenantId the tenant ID
     * @param userId the user ID
     * @return the new semantic memory content
     */
    public String summarizeMemories(List<MemoryContent> memories, UUID tenantId, UUID userId) {
        if (memories.isEmpty()) {
            return "";
        }

        if (memories.size() == 1) {
            return memories.get(0).content();
        }

        log.info("Summarizing {} memories for tenantId={}, userId={}",
                memories.size(), tenantId, userId);

        // In production: use LangChain4j to call LLM for summarization
        // Prompt: "Summarize these memories into a single coherent memory..."

        String summary = generateSimpleSummary(memories);

        log.info("Summary generated (length={})", summary.length());
        return summary;
    }

    /**
     * Simple summary generation for development.
     * In production, use LangChain4j with actual LLM.
     */
    private String generateSimpleSummary(List<MemoryContent> memories) {
        StringBuilder sb = new StringBuilder();
        sb.append("Summary of ").append(memories.size()).append(" memories: ");

        // Combine first parts of each memory
        for (int i = 0; i < Math.min(3, memories.size()); i++) {
            String content = memories.get(i).content();
            if (content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            sb.append(content).append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Creates a new semantic memory from summarized content.
     *
     * @param summary the summarized content
     * @param tenantId the tenant ID
     * @param userId the user ID
     * @param sourceConversationId source conversation for provenance
     * @return the new memory ID
     */
    public UUID createSemanticMemory(String summary, UUID tenantId, UUID userId, UUID sourceConversationId) {
        log.info("Creating semantic memory for tenantId={}, userId={}", tenantId, userId);

        // In production: persist to database with memoryType = SEMANTIC
        UUID newMemoryId = UUID.randomUUID();

        log.info("Semantic memory created: memoryId={}", newMemoryId);
        return newMemoryId;
    }

    public record MemoryContent(UUID memoryId, String content) {}
}