package com.aimemory.retrieval.api;

import com.aimemory.retrieval.pipeline.ContextBudgetAllocator;
import com.aimemory.retrieval.pipeline.HybridSearchService;
import com.aimemory.retrieval.pipeline.QueryRewriterService;
import com.aimemory.retrieval.pipeline.ReRankingService;
import com.aimemory.retrieval.pipeline.RetrievalPipelineService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for retrieval operations.
 * Provides an alternative to gRPC for HTTP-based retrieval.
 *
 * @author agent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/retrieval")
public class RetrievalController {

    private final RetrievalPipelineService pipelineService;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RetrievalController(
            RetrievalPipelineService pipelineService,
            ReactiveRedisTemplate<String, String> redisTemplate) {
        this.pipelineService = pipelineService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Retrieve memories for a query.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> retrieve(
            @RequestBody RetrievalRequest request,
            @RequestHeader("X-Tenant-Id") String tenantIdHeader,
            @RequestHeader("X-User-Id") String userIdHeader) {

        UUID tenantId = UUID.fromString(tenantIdHeader);
        UUID userId = UUID.fromString(userIdHeader);

        var memories = pipelineService.retrieve(
                request.query(),
                tenantId,
                userId,
                request.tokenBudget()
        );

        var results = memories.stream()
                .map(m -> Map.of(
                        "memoryId", m.memoryId().toString(),
                        "content", m.content(),
                        "score", m.score(),
                        "memoryType", m.memoryType(),
                        "importanceScore", m.importanceScore()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "memories", results,
                "count", results.size(),
                "tokenBudget", request.tokenBudget()
        ));
    }

    public record RetrievalRequest(String query, int tokenBudget) {
    }
}