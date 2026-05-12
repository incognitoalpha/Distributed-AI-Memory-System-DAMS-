package com.aimemory.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Agent Service - orchestrates AI agents with memory context.
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.aimemory")
public class AgentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentServiceApplication.class, args);
    }
}