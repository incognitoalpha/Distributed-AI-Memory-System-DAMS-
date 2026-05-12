package com.aimemory.memory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Memory Service - handles memory CRUD operations, versioning, and conflict resolution.
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.aimemory")
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
public class MemoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoryServiceApplication.class, args);
    }
}