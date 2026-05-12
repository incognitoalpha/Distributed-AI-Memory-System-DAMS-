package com.aimemory.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantInjectionFilterTest {

    private TenantInjectionFilter tenantInjectionFilter;
    private GatewayFilterChain mockChain;

    @BeforeEach
    void setUp() {
        tenantInjectionFilter = new TenantInjectionFilter();
        mockChain = mock(GatewayFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_does_nothing_when_no_attributes_set() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/memories").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // No attributes set
        tenantInjectionFilter.filter(exchange, mockChain);

        // Should call chain
        verify(mockChain).filter(exchange);
    }

    @Test
    void filter_continues_chain_when_only_tenant_set() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/memories").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        exchange.getAttributes().put("tenantId", UUID.randomUUID());

        tenantInjectionFilter.filter(exchange, mockChain);

        // Should still call chain
        verify(mockChain).filter(exchange);
    }

    @Test
    void filter_has_order_value() {
        // Verify the filter has an order value
        int order = tenantInjectionFilter.getOrder();
        assertNotEquals(0, order, "Filter order should not be zero");
    }

    @Test
    void filter_constants_are_defined() {
        assertEquals("X-Tenant-Id", TenantInjectionFilter.TENANT_HEADER);
        assertEquals("X-User-Id", TenantInjectionFilter.USER_HEADER);
        assertEquals("X-User-Roles", TenantInjectionFilter.ROLES_HEADER);
    }

    @Test
    void filter_handles_different_paths() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/other").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // No attributes set - should pass through
        tenantInjectionFilter.filter(exchange, mockChain);
        verify(mockChain).filter(exchange);
    }
}