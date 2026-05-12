package com.aimemory.shared.util;

import org.springframework.stereotype.Component;

/**
 * Utility for calculating and managing token budgets.
 * Uses tiktoken-approximation logic for token counting.
 * This implementation provides a reasonable approximation without requiring the actual tiktoken library.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class TokenBudgetCalculator {

    private static final double TOKENS_PER_CHAR_APPROXIMATION = 0.25;
    private static final double TOKENS_PER_WORD_APPROXIMATION = 1.3;

    public TokenBudgetCalculator() {
    }

    /**
     * Estimates token count for a given text using character-based approximation.
     * This is a rough approximation - actual token count varies by content.
     *
     * @param text the input text
     * @return estimated token count
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() * TOKENS_PER_CHAR_APPROXIMATION);
    }

    /**
     * Estimates token count for a given text using word-based approximation.
     * Generally more accurate for natural language text.
     *
     * @param text the input text
     * @return estimated token count
     */
    public int estimateTokensByWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String[] words = text.split("\\s+");
        return (int) Math.ceil(words.length * TOKENS_PER_WORD_APPROXIMATION);
    }

    /**
     * Checks if a list of texts fits within the given token budget.
     *
     * @param texts       the list of texts to check
     * @param tokenBudget the maximum token budget
     * @return true if all texts fit within the budget
     */
    public boolean fitsInBudget(Iterable<String> texts, int tokenBudget) {
        int totalTokens = 0;
        for (String text : texts) {
            totalTokens += estimateTokens(text);
            if (totalTokens > tokenBudget) {
                return false;
            }
        }
        return true;
    }

    /**
     * Trims texts to fit within the token budget, preferring higher-ranked texts.
     *
     * @param texts       list of texts in ranked order
     * @param tokenBudget the maximum token budget
     * @return list of texts that fit within the budget
     */
    public java.util.List<String> trimToBudget(java.util.List<String> texts, int tokenBudget) {
        java.util.List<String> result = new java.util.ArrayList<>();
        int usedTokens = 0;

        for (String text : texts) {
            int textTokens = estimateTokens(text);
            if (usedTokens + textTokens > tokenBudget) {
                break;
            }
            result.add(text);
            usedTokens += textTokens;
        }

        return result;
    }

    /**
     * Calculates how many more tokens can be allocated.
     *
     * @param used    tokens already used
     * @param budget  total token budget
     * @return remaining token budget
     */
    public int remainingBudget(int used, int budget) {
        return Math.max(0, budget - used);
    }
}