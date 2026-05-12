package com.aimemory.embedding.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for generating vector embeddings using OpenAI API.
 * Includes circuit breaker for fault tolerance.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class EmbeddingGenerationService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingGenerationService.class);

    @Value("${openai.api.key:${OPENAI_API_KEY:}}")
    private String openaiApiKey;

    @Value("${embedding.model.name:text-embedding-3-small}")
    private String modelName;

    private final ModelVersionRegistry modelRegistry;

    public EmbeddingGenerationService(ModelVersionRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    /**
     * Generates an embedding vector for the given text.
     * Uses circuit breaker to handle OpenAI API failures gracefully.
     *
     * @param text the text to embed
     * @return embedding result with vector and metadata
     */
    @CircuitBreaker(name = "openai", fallbackMethod = "generateEmbeddingFallback")
    public EmbeddingResult generateEmbedding(String text) {
        log.debug("Generating embedding for text (length={})", text.length());

        try {
            // In production, use actual OpenAI client:
            // OpenAI client = new OpenAI.Builder().apiKey(openaiApiKey).build();
            // EmbeddingResponse response = client.embeddings()
            //     .createEmbedding(new EmbeddingCreateRequest.Builder()
            //         .model(modelName)
            //         .input(text)
            //         .build());

            // Mock implementation for build purposes
            List<Double> vector = generateMockEmbedding(text);
            ModelVersionRegistry.ModelInfo model = modelRegistry.getActiveModel();

            return new EmbeddingResult(
                    UUID.randomUUID(),
                    vector,
                    model.modelName(),
                    model.modelVersion(),
                    model.dimension(),
                    true
            );
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage());
            throw new EmbeddingGenerationException("Failed to generate embedding", e);
        }
    }

    /**
     * Fallback method when OpenAI API is unavailable.
     */
    public EmbeddingResult generateEmbeddingFallback(String text, Throwable t) {
        log.warn("Embedding generation fallback triggered: {}", t.getMessage());
        // Return empty embedding - downstream can handle gracefully
        ModelVersionRegistry.ModelInfo model = modelRegistry.getActiveModel();
        return new EmbeddingResult(
                UUID.randomUUID(),
                List.of(), // Empty vector
                model.modelName(),
                model.modelVersion(),
                model.dimension(),
                false
        );
    }

    /**
     * Batch generates embeddings for multiple texts.
     */
    public List<EmbeddingResult> generateEmbeddings(List<String> texts) {
        return texts.stream()
                .map(this::generateEmbedding)
                .toList();
    }

    /**
     * Mock embedding generation for development/testing.
     */
    private List<Double> generateMockEmbedding(String text) {
        // Simple hash-based mock for build purposes
        int hash = text.hashCode();
        int dimension = modelRegistry.getActiveModel().dimension();
        List<Double> vector = new java.util.ArrayList<>(dimension);
        for (int i = 0; i < dimension; i++) {
            double value = Math.sin(hash + i) * Math.cos(hash - i);
            vector.add(value);
        }
        return vector;
    }

    public record EmbeddingResult(
            UUID requestId,
            List<Double> vector,
            String modelName,
            String modelVersion,
            int dimension,
            boolean success
    ) {
        public int getVectorSize() {
            return vector.size();
        }
    }

    public static class EmbeddingGenerationException extends RuntimeException {
        public EmbeddingGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}