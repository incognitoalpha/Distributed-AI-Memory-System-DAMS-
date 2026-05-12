package com.aimemory.retrieval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Retrieval Service - handles memory retrieval via gRPC with hybrid search.
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.aimemory")
public class RetrievalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrievalServiceApplication.class, args);
    }
}