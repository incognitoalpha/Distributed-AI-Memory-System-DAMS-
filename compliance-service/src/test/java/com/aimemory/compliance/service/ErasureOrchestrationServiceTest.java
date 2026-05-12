package com.aimemory.compliance.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErasureOrchestrationServiceTest {

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private MeterRegistry meterRegistry;

    private ErasureOrchestrationService erasureService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        erasureService = new ErasureOrchestrationService(kafkaTemplate, jdbcTemplate, meterRegistry);

        // Default: Kafka send succeeds
        when(kafkaTemplate.send(anyString(), anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void initiateErasure_completesSuccessfully() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        // Act
        erasureService.initiateErasure(tenantId, userId, requestId);

        // Assert - verify audit event is published
        verify(kafkaTemplate).send(eq("audit.event"), eq(userId.toString()), anyMap());
    }

    @Test
    void initiateErasure_queuesVectorDeletion() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        // Act
        erasureService.initiateErasure(tenantId, userId, requestId);

        // Assert - verify vector deletion is queued
        verify(kafkaTemplate).send(eq("compliance.vector-deletion"), eq(userId.toString()), anyMap());
    }

    @Test
    void initiateErasure_queuesSearchDeletion() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        // Act
        erasureService.initiateErasure(tenantId, userId, requestId);

        // Assert - verify search deletion is queued
        verify(kafkaTemplate).send(eq("compliance.search-deletion"), eq(userId.toString()), anyMap());
    }

    @Test
    void completeErasure_publishesCompletionEvent() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        // Act
        erasureService.completeErasure(tenantId, userId, requestId);

        // Assert - verify completion audit event is published
        verify(kafkaTemplate).send(eq("audit.event"), eq(userId.toString()), anyMap());
    }
}