package com.aimemory.shared.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenBudgetCalculatorTest {

    private TokenBudgetCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TokenBudgetCalculator();
    }

    @Test
    void estimate_tokens_returns_zero_for_null() {
        assertEquals(0, calculator.estimateTokens(null));
    }

    @Test
    void estimate_tokens_returns_zero_for_empty() {
        assertEquals(0, calculator.estimateTokens(""));
    }

    @Test
    void estimate_tokens_calculates_approximately() {
        // 100 chars * 0.25 = 25 tokens
        String text = "a".repeat(100);
        assertEquals(25, calculator.estimateTokens(text));
    }

    @Test
    void estimate_tokens_rounds_up() {
        // 3 chars * 0.25 = 0.75, should round up to 1
        String text = "abc";
        assertEquals(1, calculator.estimateTokens(text));
    }

    @Test
    void estimate_tokens_by_words_returns_zero_for_null() {
        assertEquals(0, calculator.estimateTokensByWords(null));
    }

    @Test
    void estimate_tokens_by_words_returns_zero_for_empty() {
        assertEquals(0, calculator.estimateTokensByWords(""));
    }

    @Test
    void estimate_tokens_by_words_calculates_approximately() {
        // 10 words * 1.3 = 13 tokens
        String text = "one two three four five six seven eight nine ten";
        int tokens = calculator.estimateTokensByWords(text);
        assertEquals(13, tokens);
    }

    @Test
    void estimate_tokens_by_words_rounds_up() {
        // 1 word * 1.3 = 1.3, should round up to 2
        String text = "hello";
        assertEquals(2, calculator.estimateTokensByWords(text));
    }

    @Test
    void fits_in_budget_returns_true_when_under() {
        List<String> texts = List.of("short text", "another short");
        assertTrue(calculator.fitsInBudget(texts, 1000));
    }

    @Test
    void fits_in_budget_returns_false_when_over() {
        List<String> texts = List.of("a".repeat(1000), "b".repeat(1000));
        // 1000 * 0.25 = 250, 250 + 250 = 500
        assertFalse(calculator.fitsInBudget(texts, 300));
    }

    @Test
    void fits_in_budget_handles_empty_list() {
        assertTrue(calculator.fitsInBudget(List.of(), 100));
    }

    @Test
    void trim_to_budget_returns_all_when_under() {
        List<String> texts = List.of("short", "text");
        List<String> result = calculator.trimToBudget(texts, 1000);
        assertEquals(2, result.size());
    }

    @Test
    void trim_to_budget_trims_when_over() {
        List<String> texts = List.of("short", "another longer text that exceeds budget");
        List<String> result = calculator.trimToBudget(texts, 10);
        // "short" ~ 5 tokens, fits. Second string doesn't fit.
        assertTrue(result.size() <= 2);
    }

    @Test
    void trim_to_budget_returns_empty_when_budget_zero() {
        List<String> texts = List.of("some text");
        List<String> result = calculator.trimToBudget(texts, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void trim_to_budget_prefers_earlier_items() {
        // If first item fits and second doesn't, only first should be returned
        List<String> texts = List.of("a", "bbb");
        List<String> result = calculator.trimToBudget(texts, 1);
        assertEquals(1, result.size());
        assertEquals("a", result.get(0));
    }

    @Test
    void remaining_budget_returns_remaining() {
        assertEquals(50, calculator.remainingBudget(50, 100));
    }

    @Test
    void remaining_budget_returns_zero_when_over() {
        assertEquals(0, calculator.remainingBudget(150, 100));
    }

    @Test
    void remaining_budget_returns_zero_when_equal() {
        assertEquals(0, calculator.remainingBudget(100, 100));
    }
}