package com.aimemory.embedding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Embedding Service - generates vector embeddings for memories.
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.aimemory")
public class EmbeddingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddingServiceApplication.class, args);
    }
}