# Graph Report - Distributed-AI-Memory-System-DAMS-  (2026-06-04)

## Corpus Check
- 138 files · ~36,000 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 757 nodes · 1086 edges · 51 communities detected
- Extraction: 64% EXTRACTED · 36% INFERRED · 0% AMBIGUOUS · INFERRED: 395 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `30fd9896`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]

## God Nodes (most connected - your core abstractions)
1. `Memory` - 38 edges
2. `MemoryConflict` - 19 edges
3. `MemoryVersion` - 15 edges
4. `MemoryWriteServiceTest` - 15 edges
5. `from()` - 14 edges
6. `TenantContextTest` - 12 edges
7. `JsonUtilsTest` - 12 edges
8. `ContextBuilderServiceTest` - 9 edges
9. `ErasureOrchestrationService` - 9 edges
10. `ModelVersionRegistryTest` - 9 edges

## Surprising Connections (you probably didn't know these)
- `RetrievalException` --extends--> `RuntimeException`  [EXTRACTED]
  retrieval-service/src/main/java/com/aimemory/retrieval/pipeline/RetrievalPipelineService.java →   _Bridges community 8 → community 2_
- `AiMemoryException` --extends--> `RuntimeException`  [EXTRACTED]
  shared-lib/src/main/java/com/aimemory/shared/exception/AiMemoryException.java →   _Bridges community 8 → community 7_

## Communities (72 total, 37 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.06
Nodes (7): Memory, MemoryTest, DtoTest, from(), RetrievalGrpcServer, MemoryEventPublisher, MemoryEventPublisherTest

### Community 1 - "Community 1"
Cohesion: 0.07
Nodes (7): MemoryConflict, MemoryConflictTest, from(), MemoryConflictRepository, MemoryRepository, ConflictResolutionService, ConflictResolutionServiceTest

### Community 2 - "Community 2"
Cohesion: 0.05
Nodes (10): RetrievalController, MemoryRankingConsumer, ContextBudgetAllocator, ContextBudgetAllocatorTest, ReRankingService, ReRankingServiceTest, RetrievalException, RetrievalPipelineService (+2 more)

### Community 3 - "Community 3"
Cohesion: 0.06
Nodes (7): AgentController, RetrievalServiceGrpcClient, RetrievalServiceResilienceTest, AgentOrchestrationService, AgentOrchestrationServiceTest, ContextBuilderService, ContextBuilderServiceTest

### Community 4 - "Community 4"
Cohesion: 0.06
Nodes (10): DatabaseConfig, TenantAwareDataSource, TenantRlsIntegrationTest, TenantRlsInterceptor, TenantRlsStatementInspector, DelegatingDataSource, TenantContext, TenantContextTest (+2 more)

### Community 5 - "Community 5"
Cohesion: 0.06
Nodes (5): MemoryController, ErasureOrchestrationService, ErasureOrchestrationServiceTest, MemoryWriteService, MemoryWriteServiceTest

### Community 6 - "Community 6"
Cohesion: 0.08
Nodes (5): MemoryVersion, MemoryVersionTest, MemoryVersionRepository, MemoryVersionService, MemoryVersionServiceTest

### Community 7 - "Community 7"
Cohesion: 0.08
Nodes (7): AiMemoryException, AiMemoryException, AiMemoryExceptionTest, TestException, GlobalExceptionHandler, MemoryNotFoundException, TenantIsolationException

### Community 8 - "Community 8"
Cohesion: 0.1
Nodes (9): RateLimitConfig, JwtAuthFilter, RateLimitExceededException, RateLimitFilter, TenantInjectionFilter, GlobalFilter, Ordered, RuntimeException (+1 more)

### Community 9 - "Community 9"
Cohesion: 0.17
Nodes (3): getFullVersion(), ModelVersionRegistry, ModelVersionRegistryTest

### Community 10 - "Community 10"
Cohesion: 0.13
Nodes (3): PruningScheduler, SoftDeleteService, SoftDeleteServiceTest

### Community 13 - "Community 13"
Cohesion: 0.15
Nodes (3): EmbeddingGenerationService, getVectorSize(), EmbeddingGenerationServiceTest

### Community 14 - "Community 14"
Cohesion: 0.16
Nodes (3): ReindexBatchJob, ReindexClient, ReindexItemReader

### Community 27 - "Community 27"
Cohesion: 0.38
Nodes (4): EmbeddingRequestConsumer, memoryIdAsUuid(), tenantIdAsUuid(), userIdAsUuid()

## Knowledge Gaps
- **37 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AiMemoryException` connect `Community 7` to `Community 8`?**
  _High betweenness centrality (0.148) - this node is a cross-community bridge._
- **Why does `EmbeddingGenerationException` connect `Community 8` to `Community 13`?**
  _High betweenness centrality (0.127) - this node is a cross-community bridge._
- **Why does `EmbeddingGenerationService` connect `Community 13` to `Community 20`?**
  _High betweenness centrality (0.118) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `from()` (e.g. with `.getMemoryType()` and `.getSourceConversationId()`) actually correct?**
  _`from()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._