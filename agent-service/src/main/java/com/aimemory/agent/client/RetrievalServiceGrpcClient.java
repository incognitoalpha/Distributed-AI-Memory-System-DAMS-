package com.aimemory.agent.client;

import com.aimemory.shared.proto.RetrievalRequest;
import com.aimemory.shared.proto.RetrievalResponse;
import com.aimemory.shared.proto.RetrievalServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client for communicating with the Retrieval Service via gRPC.
 * Replaces the previous REST-based implementation for high performance.
 * Includes circuit breaking for high availability.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class RetrievalServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(RetrievalServiceGrpcClient.class);

    private final RetrievalServiceGrpc.RetrievalServiceBlockingStub retrievalStub;

    public RetrievalServiceGrpcClient(@GrpcClient("retrievalService") RetrievalServiceGrpc.RetrievalServiceBlockingStub retrievalStub) {
        this.retrievalStub = retrievalStub;
    }

    /**
     * Retrieves memories for a given query via gRPC.
     * Wrapped in a circuit breaker to prevent cascading failures.
     *
     * @param query the user query
     * @param tenantId the tenant ID
     * @param userId the user ID
     * @param tokenBudget the maximum tokens for context
     * @return list of retrieved memories
     */
    @CircuitBreaker(name = "retrievalService", fallbackMethod = "fallbackRetrieveMemories")
    public List<RetrievedMemory> retrieveMemories(String query, UUID tenantId, UUID userId, int tokenBudget) {
        log.debug("Retrieving memories via gRPC: query={}, tenantId={}, userId={}, tokenBudget={}",
                query, tenantId, userId, tokenBudget);

        RetrievalRequest request = RetrievalRequest.newBuilder()
                .setQuery(query)
                .setTenantId(tenantId.toString())
                .setUserId(userId.toString())
                .setTokenBudget(tokenBudget)
                .setTraceId(UUID.randomUUID().toString())
                .build();

        RetrievalResponse response = retrievalStub.retrieve(request);

        return response.getMemoriesList().stream()
                .map(m -> new RetrievedMemory(
                        UUID.fromString(m.getMemoryId()),
                        m.getContent(),
                        m.getScore(),
                        m.getMemoryType(),
                        m.getImportanceScore()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Fallback method for retrieveMemories when the circuit is open or retrieval fails.
     */
    public List<RetrievedMemory> fallbackRetrieveMemories(String query, UUID tenantId, UUID userId, int tokenBudget, Throwable t) {
        log.warn("Retrieval gRPC fallback triggered for userId={}. Reason: {}", userId, t.getMessage());
        return List.of(); // Return empty context to allow LLM to proceed without long-term memory
    }

    public record RetrievedMemory(
            UUID memoryId,
            String content,
            double score,
            String memoryType,
            double importanceScore
    ) {}
}
