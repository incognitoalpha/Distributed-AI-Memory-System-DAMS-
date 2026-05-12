package com.aimemory.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Rate limiting configuration for the gateway.
 * Per-tenant rate limits are enforced via Redis.
 *
 * @author agent
 * @since 1.0.0
 */
@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-second:100}")
    private int requestsPerSecond;

    @Value("${rate-limit.burst-capacity:150}")
    private int burstCapacity;

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public int getBurstCapacity() {
        return burstCapacity;
    }
}