package com.aimemory.ranking.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Scores memory importance based on entity salience.
 * Uses simple regex patterns to detect named entities for MVP.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class EntitySalienceScorer {

    // Simple patterns for named entity detection
    private static final Pattern CAPITALIZED_WORDS = Pattern.compile("\\b[A-Z][a-z]+\\b");
    private static final Pattern EMAIL = Pattern.compile("\\b[\\w.-]+@[\\w.-]+\\.\\w+\\b");
    private static final Pattern URL = Pattern.compile("https?://\\S+");
    private static final Pattern NUMBER = Pattern.compile("\\b\\d{4,}\\b"); // Years, large numbers

    // Stop words to filter out
    private static final Set<String> STOP_WORDS = Set.of(
            "The", "This", "That", "These", "Those",
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday", "Sunday", "January", "February", "March",
            "April", "May", "June", "July", "August",
            "September", "October", "November", "December"
    );

    /**
     * Computes salience score based on entity density.
     * Higher density of named entities = higher salience.
     *
     * @param content the memory content
     * @return score between 0.0 and 1.0
     */
    public double computeScore(String content) {
        if (content == null || content.isBlank()) {
            return 0.0;
        }

        // Extract potential entities
        Set<String> entities = new HashSet<>();

        // Find capitalized words (potential names, places, organizations)
        var matcher = CAPITALIZED_WORDS.matcher(content);
        while (matcher.find()) {
            String word = matcher.group();
            if (!STOP_WORDS.contains(word) && word.length() > 2) {
                entities.add(word.toLowerCase());
            }
        }

        // Check for emails
        if (EMAIL.matcher(content).find()) {
            entities.add("email");
        }

        // Check for URLs
        if (URL.matcher(content).find()) {
            entities.add("url");
        }

        // Check for numbers (dates, quantities)
        if (NUMBER.matcher(content).find()) {
            entities.add("number");
        }

        // Score based on entity density relative to content length
        int wordCount = content.split("\\s+").length;
        double entityDensity = entities.size() / (double) Math.max(1, wordCount);

        // Normalize to 0-1 range
        return Math.min(1.0, entityDensity * 10);
    }
}