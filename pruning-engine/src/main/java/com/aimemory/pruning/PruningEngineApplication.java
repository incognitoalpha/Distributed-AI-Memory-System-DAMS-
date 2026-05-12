package com.aimemory.pruning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Pruning Engine - handles memory lifecycle (soft-delete, summarization, hard-delete).
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication
public class PruningEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(PruningEngineApplication.class, args);
    }
}