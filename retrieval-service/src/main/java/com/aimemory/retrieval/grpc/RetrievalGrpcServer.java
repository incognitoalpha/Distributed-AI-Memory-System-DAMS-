package com.aimemory.retrieval.grpc;

import com.aimemory.retrieval.pipeline.RetrievalPipelineService;
import com.aimemory.shared.proto.MemoryResult;
import com.aimemory.shared.proto.RetrievalRequest;
import com.aimemory.shared.proto.RetrievalResponse;
import com.aimemory.shared.proto.RetrievalServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * gRPC server implementation for the Retrieval Service.
 * Exposes high-performance endpoints for context retrieval.
 *
 * @author agent
 * @since 1.0.0
 */
@GrpcService
public class RetrievalGrpcServer extends RetrievalServiceGrpc.RetrievalServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(RetrievalGrpcServer.class);

    private final RetrievalPipelineService pipelineService;

    public RetrievalGrpcServer(RetrievalPipelineService pipelineService) {
        this.pipelineService = pipelineService;
        log.info("RetrievalGrpcServer initialized and registered as @GrpcService");
    }

    @Override
    public void retrieve(RetrievalRequest request, StreamObserver<RetrievalResponse> responseObserver) {
        log.debug("Received gRPC retrieval request: query={}, tenantId={}, userId={}",
                request.getQuery(), request.getTenantId(), request.getUserId());

        try {
            // Convert gRPC request to domain calls
            UUID tenantId = UUID.fromString(request.getTenantId());
            UUID userId = UUID.fromString(request.getUserId());
            
            var rankedMemories = pipelineService.retrieve(
                    request.getQuery(),
                    tenantId,
                    userId,
                    request.getTokenBudget()
            );

            // Map results back to gRPC response
            RetrievalResponse response = RetrievalResponse.newBuilder()
                    .addAllMemories(rankedMemories.stream()
                            .map(m -> MemoryResult.newBuilder()
                                    .setMemoryId(m.memoryId().toString())
                                    .setContent(m.content())
                                    .setScore(m.score())
                                    .setMemoryType(m.memoryType())
                                    .setImportanceScore(m.importanceScore())
                                    .build())
                            .collect(Collectors.toList()))
                    .setTraceId(request.getTraceId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC retrieval failed: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Retrieval failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
