package com.aimemory.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that injects tenant and user IDs as headers for downstream services.
 * Reads from exchange attributes set by JwtAuthFilter.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class TenantInjectionFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TenantInjectionFilter.class);

    public static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String USER_HEADER = "X-User-Id";
    public static final String ROLES_HEADER = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        UUID tenantId = exchange.getAttribute("tenantId");
        UUID userId = exchange.getAttribute("userId");
        @SuppressWarnings("unchecked")
        java.util.List<String> roles = exchange.getAttribute("roles");

        if (tenantId != null && userId != null) {
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(TENANT_HEADER, tenantId.toString())
                    .header(USER_HEADER, userId.toString())
                    .header(ROLES_HEADER, roles != null ? String.join(",", roles) : "")
                    .build();

            log.debug("Injected headers: tenantId={} userId={}", tenantId, userId);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run after JwtAuthFilter
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}