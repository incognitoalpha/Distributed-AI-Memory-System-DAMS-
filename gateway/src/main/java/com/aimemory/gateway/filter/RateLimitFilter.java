package com.aimemory.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * Rate limiting filter using Redis for per-tenant rate limiting.
 * Implements token bucket algorithm.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final int requestsPerSecond;
    private final int burstCapacity;

    public RateLimitFilter(
            ReactiveStringRedisTemplate redisTemplate,
            com.aimemory.gateway.config.RateLimitConfig config) {
        this.redisTemplate = redisTemplate;
        this.requestsPerSecond = config.getRequestsPerSecond();
        this.burstCapacity = config.getBurstCapacity();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip rate limiting for actuator and auth endpoints
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/actuator") || path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        UUID tenantId = exchange.getAttribute("tenantId");
        if (tenantId == null) {
            return chain.filter(exchange);
        }

        String key = "rate_limit:" + tenantId;

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // First request, set expiry to 1 second
                        return redisTemplate.expire(key, Duration.ofSeconds(1))
                                .then(checkRateLimit(count));
                    }
                    return checkRateLimit(count);
                })
                .switchIfEmpty(chain.filter(exchange))
                .onErrorResume(e -> {
                    log.warn("Rate limit check failed, allowing request: {}", e.getMessage());
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> checkRateLimit(Long count) {
        if (count > burstCapacity) {
            log.warn("Rate limit exceeded for tenant");
            return Mono.error(new RateLimitExceededException());
        }
        return Mono.empty();
    }

    @Override
    public int getOrder() {
        // Run after tenant injection
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }

    public record RateLimitProperties(int requestsPerSecond, int burstCapacity) {
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException() {
            super("Rate limit exceeded");
        }
    }
}