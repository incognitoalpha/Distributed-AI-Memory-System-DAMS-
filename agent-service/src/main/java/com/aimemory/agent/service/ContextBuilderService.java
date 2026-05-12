package com.aimemory.agent.service;

import com.aimemory.agent.client.RetrievalServiceGrpcClient;
import com.aimemory.shared.util.TokenBudgetCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Builds context prompts by injecting retrieved memories into the LLM prompt.
 * Handles memory conflict annotations and token budget allocation.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ContextBuilderService {

    private static final Logger log = LoggerFactory.getLogger(ContextBuilderService.class);

    private static final String SYSTEM_PROMPT = """
            You are an AI assistant with access to memory context.
            The memory context contains relevant information from the user's past conversations and knowledge.
            Use this context to provide more informed and personalized responses.
            If the memories contain contradictory information, note this in your response.
            """;

    private static final int DEFAULT_TOKEN_BUDGET = 4000;

    private final TokenBudgetCalculator tokenCalculator;

    public ContextBuilderService(TokenBudgetCalculator tokenCalculator) {
        this.tokenCalculator = tokenCalculator;
    }

    /**
     * Builds a complete prompt with memory context injected.
     *
     * @param userMessage the user's message
     * @param retrievedMemories memories retrieved from the Retrieval Service
     * @return complete prompt string
     */
    public String buildPrompt(String userMessage, List<RetrievalServiceGrpcClient.RetrievedMemory> retrievedMemories) {
        if (retrievedMemories.isEmpty()) {
            return SYSTEM_PROMPT + "\n\nUser: " + userMessage;
        }

        // Build memory context section
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("## Memory Context\n\n");

        for (int i = 0; i < retrievedMemories.size(); i++) {
            RetrievalServiceGrpcClient.RetrievedMemory memory = retrievedMemories.get(i);
            String annotation = "";

            // Annotate potential conflicts
            if (i > 0 && isPotentialConflict(retrievedMemories.get(i - 1), memory)) {
                annotation = " [CONFLICT: potentially contradicts previous memory]";
            }

            contextBuilder.append(String.format("%d. %s (relevance: %.2f)%s\n",
                    i + 1,
                    memory.content(),
                    memory.score(),
                    annotation
            ));
        }

        contextBuilder.append("\n## End Memory Context\n\n");

        // Check token budget
        String context = contextBuilder.toString();
        int contextTokens = tokenCalculator.estimateTokens(context);
        int userMessageTokens = tokenCalculator.estimateTokens(userMessage);
        int totalTokens = contextTokens + userMessageTokens + 500; // 500 for response buffer

        if (totalTokens > DEFAULT_TOKEN_BUDGET) {
            log.warn("Context exceeds token budget: {} > {}", totalTokens, DEFAULT_TOKEN_BUDGET);
            // Trim memories to fit budget
            context = trimToBudget(contextBuilder.toString(), retrievedMemories, DEFAULT_TOKEN_BUDGET - userMessageTokens - 500);
        }

        return SYSTEM_PROMPT + "\n\n" + context + "\n\nUser: " + userMessage;
    }

    private boolean isPotentialConflict(RetrievalServiceGrpcClient.RetrievedMemory m1, RetrievalServiceGrpcClient.RetrievedMemory m2) {
        // Simple heuristic - check if memories have similar content but different scores
        return Math.abs(m1.score() - m2.score()) > 0.3;
    }

    private String trimToBudget(String context, List<RetrievalServiceGrpcClient.RetrievedMemory> memories, int budget) {
        StringBuilder result = new StringBuilder();
        result.append("## Memory Context (trimmed)\n\n");

        int usedTokens = 0;
        for (int i = 0; i < memories.size(); i++) {
            String content = memories.get(i).content();
            int tokens = tokenCalculator.estimateTokens(content);

            if (usedTokens + tokens > budget) {
                break;
            }

            result.append(String.format("%d. %s\n", i + 1, content));
            usedTokens += tokens;
        }

        result.append("\n## End Memory Context\n\n");
        return result.toString();
    }
}