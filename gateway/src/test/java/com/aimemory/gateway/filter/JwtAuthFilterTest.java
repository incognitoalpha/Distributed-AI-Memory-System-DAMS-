package com.aimemory.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private static final String SECRET = "this-is-a-32-character-secret-key!!";
    private JwtAuthFilter jwtAuthFilter;
    private GatewayFilterChain mockChain;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(SECRET);
        mockChain = mock(GatewayFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_passes_for_health_endpoint() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void filter_passes_for_auth_endpoint() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/auth/token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void filter_returns_401_for_missing_auth_header() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/memories").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, mockChain);

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_returns_401_for_invalid_bearer_token() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/memories")
                .header("Authorization", "Bearer not.a.valid.jwt")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, mockChain);

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_passes_for_valid_token() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        // Create token valid for 1 hour
        String validToken = Jwts.builder()
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("roles", List.of("ROLE_USER"))
                .expiration(java.sql.Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/memories")
                .header("Authorization", "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, mockChain);

        // Should pass validation and call chain
        verify(mockChain).filter(exchange);
        assertNotNull(exchange.getAttribute("tenantId"));
        assertNotNull(exchange.getAttribute("userId"));
    }
}