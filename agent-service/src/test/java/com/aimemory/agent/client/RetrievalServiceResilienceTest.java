package com.aimemory.agent.client;

import com.aimemory.shared.proto.RetrievalServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class RetrievalServiceResilienceTest {

    @Autowired
    private RetrievalServiceGrpcClient retrievalClient;

    @MockBean
    private RetrievalServiceGrpc.RetrievalServiceBlockingStub retrievalStub;

    @Test
    void retrieveMemories_whenGrpcFails_triggersFallback() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String query = "test query";

        // Mock gRPC failure
        when(retrievalStub.retrieve(any()))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE.withDescription("Service down")));

        // Act
        List<RetrievalServiceGrpcClient.RetrievedMemory> result = 
                retrievalClient.retrieveMemories(query, tenantId, userId, 1000);

        // Assert
        assertThat(result).isEmpty(); // Fallback returns empty list
    }
}
