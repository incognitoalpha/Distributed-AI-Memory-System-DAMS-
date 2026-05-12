# Enterprise AI Memory System - Technical Specification
### Core Architecture & Implementation Patterns · Version 1.0.0

---

## Table of Contents
1. [Project Overview & Core Requirements](#1-project-overview--core-requirements)
2. [System Architecture & Directory Structure](#2-system-architecture--directory-structure)
3. [Technical Stack & Constraints](#3-technical-stack--constraints)
4. [Agent Persona & Implementation Standards](#4-agent-persona--implementation-standards)
5. [Reference Code Snippets](#5-reference-code-snippets)
6. [Autonomous Roadmap — Execution Protocol](#6-autonomous-roadmap--execution-protocol)
7. [Verification Gates](#7-verification-gates)

---

## 1. Project Overview & Core Requirements

### 1.1 Executive Summary
A production-grade, enterprise-ready **Distributed AI Memory System** built on Java 21 + Spring Boot 3.x microservices. It provides AI agents with persistent long-term memory, semantic recall, session continuity, and automated memory lifecycle management (ranking, compression, pruning). Every architectural decision documented in this PRD resolves the critical gaps identified in the architecture review: read-path latency, memory model integrity, RAG pipeline quality, embedding model coupling, compliance, and observability.

### 1.2 Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-01 | Store, version, and retrieve episodic and semantic memories per user/tenant | P0 |
| FR-02 | Retrieve memories via hybrid search (dense vector + sparse BM25) with cross-encoder re-ranking | P0 |
| FR-03 | Rewrite queries using HyDE before embedding to improve retrieval precision | P0 |
| FR-04 | Score memory importance using a heuristic model (recency decay + frequency + entity salience) | P0 |
| FR-05 | Compress and summarize low-value memories; soft-delete before hard-delete with a 30-day hold | P0 |
| FR-06 | Resolve memory conflicts when newer memories contradict older ones | P1 |
| FR-07 | Enforce per-tenant and per-user memory isolation at the storage layer | P0 |
| FR-08 | Expose GDPR-compliant memory erasure and export endpoints | P0 |
| FR-09 | Track full provenance of every memory record (source conversation, session, agent) | P1 |
| FR-10 | Support embedding model versioning and trigger reindexing pipelines on model upgrade | P1 |
| FR-11 | Emit structured observability events for memory hit rate, pruning decisions, and embedding drift | P1 |
| FR-12 | Provide a synchronous gRPC retrieval path; use Kafka only on write and analytics paths | P0 |

### 1.3 Non-Functional Requirements

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-01 | Retrieval P99 latency (gRPC, cached) | < 80 ms |
| NFR-02 | Retrieval P99 latency (uncached, vector search) | < 400 ms |
| NFR-03 | Memory write throughput per tenant | 500 writes/sec |
| NFR-04 | System availability | 99.9% uptime |
| NFR-05 | Tenant data isolation | Zero cross-tenant data leakage |
| NFR-06 | GDPR erasure SLA | < 72 hours after request |
| NFR-07 | Embedding reindex pipeline completion | < 4 hours for 10M records |
| NFR-08 | Test coverage | ≥ 80% line coverage per service |
| NFR-09 | Zero critical/high Checkstyle or SonarQube violations | Enforced in CI |

---

## 2. System Architecture & Directory Structure

### 2.1 Service Map & Data Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│  CLIENT LAYER                                                       │
│  REST / WebSocket (claude.ai, third-party agents)                   │
└──────────────────────────┬──────────────────────────────────────────┘
                           │ HTTPS
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│  API GATEWAY  (Spring Cloud Gateway 4.x)                            │
│  • JWT validation  • Rate limiting  • Tenant header injection       │
└──────┬──────────────────┬────────────────────────┬──────────────────┘
       │ REST             │ REST                   │ REST
       ▼                  ▼                        ▼
┌─────────────┐  ┌────────────────┐  ┌────────────────────────────┐
│ Agent Svc   │  │  Memory Svc    │  │  Compliance Svc            │
│ (port 8081) │  │  (port 8082)   │  │  (port 8085)               │
│             │  │                │  │  GDPR erasure / export     │
│ Manages AI  │  │ CRUD + version │  │  Audit log consumer        │
│ sessions &  │  │ Conflict       │  └────────────┬───────────────┘
│ RAG context │  │ resolution     │               │
└──────┬──────┘  └───────┬────────┘               │
       │ gRPC            │ Kafka WRITE            │
       │ (sync)          │ (async)                │
       ▼                 ▼                        │
┌─────────────────────────────────────────────────────────────────────┐
│  RETRIEVAL SERVICE  (port 8083)  — READ HOT PATH                    │
│  ① HyDE query rewriting  ② Hybrid search (vector + BM25)            │
│  ③ Cross-encoder re-rank  ④ Context window budget allocation        │
└──────┬──────────────┬──────────────────┬────────────────────────────┘
       │ gRPC          │ JDBC             │ REST
       ▼               ▼                 ▼
┌──────────┐  ┌──────────────────┐  ┌──────────────────┐
│ Embedding│  │ Vector DB        │  │ Search Engine    │
│ Svc      │  │ Weaviate 1.24    │  │ OpenSearch 2.x   │
│ (8084)   │  │ (per-tenant cls) │  │ (BM25 index)     │
└──────────┘  └──────────────────┘  └──────────────────┘
       │
       │ Kafka WRITE (async)
       ▼
┌────────────────────────────────────────────────────────────────────┐
│  KAFKA EVENT BUS  (Confluent / self-hosted Kafka 3.6)              │
├──────────────────┬──────────────────┬──────────────────────────────┤
│ memory.ingested  │ memory.retrieved │ embedding.requested          │
│ memory.versioned │ memory.pruned    │ embedding.completed          │
│ memory.conflict  │ audit.event      │ reindex.triggered            │
└──────┬───────────┴──────────────────┴──────────────────────────────┘
       │
       ▼
┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│ Ranking Engine │  │ Pruning Engine │  │ Reindex Worker │
│  Heuristic     │  │  Soft-delete   │  │  Batch job     │
│  scoring       │  │  30-day hold   │  │  on model chg  │
└────────────────┘  └────────────────┘  └────────────────┘
       │                    │
       ▼                    ▼
┌────────────────────────────────────────────────────────────────────┐
│  POSTGRESQL 16  (Primary metadata store)                           │
│  • memories table (with RLS per tenant_id)                         │
│  • memory_versions, memory_conflicts, audit_log                    │
│  • pruning_queue (soft-delete staging)                             │
└────────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────┐
│  REDIS 7.2  (Hot session cache — read path only)                    │
└─────────────────────────────────────────────────────────────────────┘
```

**Critical rule:** Kafka is **never** on the synchronous retrieval hot path. All user-facing reads use gRPC directly to the Retrieval Service.

### 2.2 Canonical Directory Structure

```
distributed-ai-memory/
├── PRD.md                              ← THIS FILE (agent updates it)
├── docker-compose.yml                  ← Local dev infra
├── docker-compose.test.yml             ← Integration test infra
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── deploy.yml
├── gateway/                            ← Spring Cloud Gateway
│   ├── src/main/java/com/aimemory/gateway/
│   │   ├── config/
│   │   │   ├── GatewayConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── RateLimitConfig.java
│   │   └── filter/
│   │       ├── TenantInjectionFilter.java
│   │       └── JwtAuthFilter.java
│   └── src/test/java/com/aimemory/gateway/
├── agent-service/
│   ├── src/main/java/com/aimemory/agent/
│   │   ├── AgentServiceApplication.java
│   │   ├── api/
│   │   │   ├── AgentController.java
│   │   │   └── dto/
│   │   │       ├── ConversationRequest.java
│   │   │       └── ConversationResponse.java
│   │   ├── service/
│   │   │   ├── AgentOrchestrationService.java
│   │   │   └── ContextBuilderService.java
│   │   ├── client/
│   │   │   └── RetrievalServiceGrpcClient.java
│   │   └── config/
│   │       └── LangChain4jConfig.java
│   └── src/test/
├── memory-service/
│   ├── src/main/java/com/aimemory/memory/
│   │   ├── MemoryServiceApplication.java
│   │   ├── api/
│   │   │   ├── MemoryController.java
│   │   │   └── dto/
│   │   │       ├── MemoryCreateRequest.java
│   │   │       ├── MemoryResponse.java
│   │   │       └── MemoryConflictResponse.java
│   │   ├── domain/
│   │   │   ├── Memory.java                ← Core entity
│   │   │   ├── MemoryVersion.java
│   │   │   ├── MemoryConflict.java
│   │   │   └── enums/
│   │   │       ├── MemoryType.java
│   │   │       └── ConflictResolutionStrategy.java
│   │   ├── repository/
│   │   │   ├── MemoryRepository.java
│   │   │   ├── MemoryVersionRepository.java
│   │   │   └── MemoryConflictRepository.java
│   │   ├── service/
│   │   │   ├── MemoryWriteService.java
│   │   │   ├── MemoryVersionService.java
│   │   │   └── ConflictResolutionService.java
│   │   ├── kafka/
│   │   │   └── MemoryEventPublisher.java
│   │   └── config/
│   │       └── TenantRlsConfig.java
│   └── src/test/
├── retrieval-service/
│   ├── src/main/java/com/aimemory/retrieval/
│   │   ├── RetrievalServiceApplication.java
│   │   ├── grpc/
│   │   │   └── RetrievalGrpcServer.java
│   │   ├── pipeline/
│   │   │   ├── QueryRewriterService.java     ← HyDE implementation
│   │   │   ├── HybridSearchService.java      ← vector + BM25
│   │   │   ├── ReRankingService.java         ← cross-encoder
│   │   │   └── ContextBudgetAllocator.java
│   │   ├── client/
│   │   │   ├── WeaviateClient.java
│   │   │   └── OpenSearchClient.java
│   │   └── config/
│   └── src/test/
├── embedding-service/
│   ├── src/main/java/com/aimemory/embedding/
│   │   ├── EmbeddingServiceApplication.java
│   │   ├── api/
│   │   │   └── EmbeddingController.java
│   │   ├── service/
│   │   │   ├── EmbeddingGenerationService.java
│   │   │   └── ModelVersionRegistry.java      ← model version tracking
│   │   ├── kafka/
│   │   │   ├── EmbeddingRequestConsumer.java
│   │   │   └── EmbeddingEventPublisher.java
│   │   └── reindex/
│   │       └── ReindexBatchJob.java            ← Spring Batch
│   └── src/test/
├── ranking-engine/
│   ├── src/main/java/com/aimemory/ranking/
│   │   ├── RankingEngineApplication.java
│   │   ├── kafka/
│   │   │   └── MemoryRankingConsumer.java
│   │   ├── service/
│   │   │   ├── ImportanceScoringService.java  ← heuristic model
│   │   │   ├── RecencyDecayCalculator.java
│   │   │   ├── FrequencyScorer.java
│   │   │   └── EntitySalienceScorer.java
│   │   └── config/
│   └── src/test/
├── pruning-engine/
│   ├── src/main/java/com/aimemory/pruning/
│   │   ├── PruningEngineApplication.java
│   │   ├── kafka/
│   │   │   └── PruningEventConsumer.java
│   │   ├── service/
│   │   │   ├── PruningDecisionService.java
│   │   │   ├── SoftDeleteService.java         ← 30-day hold logic
│   │   │   └── SummarizationService.java
│   │   ├── scheduler/
│   │   │   └── PruningScheduler.java
│   │   └── config/
│   └── src/test/
├── compliance-service/
│   ├── src/main/java/com/aimemory/compliance/
│   │   ├── ComplianceServiceApplication.java
│   │   ├── api/
│   │   │   ├── GdprController.java
│   │   │   └── dto/
│   │   │       └── ErasureRequest.java
│   │   ├── service/
│   │   │   ├── ErasureOrchestrationService.java
│   │   │   └── DataExportService.java
│   │   └── audit/
│   │       └── AuditLogConsumer.java
│   └── src/test/
├── auth-service/
│   ├── src/main/java/com/aimemory/auth/
│   │   ├── AuthServiceApplication.java
│   │   ├── api/
│   │   │   └── AuthController.java
│   │   ├── service/
│   │   │   ├── TokenService.java
│   │   │   └── TenantAclService.java          ← memory-level ACL
│   │   └── config/
│   └── src/test/
├── shared-lib/                                 ← shared DTOs, utils, proto
│   ├── src/main/java/com/aimemory/shared/
│   │   ├── domain/
│   │   │   ├── TenantContext.java
│   │   │   └── AuditEvent.java
│   │   ├── exception/
│   │   │   ├── MemoryNotFoundException.java
│   │   │   ├── TenantIsolationException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   └── util/
│   │       ├── JsonUtils.java
│   │       └── TokenBudgetCalculator.java
│   └── src/main/proto/
│       └── retrieval.proto                    ← gRPC contract
├── infra/
│   ├── k8s/
│   │   ├── namespace.yaml
│   │   ├── configmaps/
│   │   ├── deployments/
│   │   └── services/
│   └── terraform/
│       ├── main.tf
│       └── variables.tf
└── docs/
    ├── adr/                                   ← Architecture Decision Records
    │   ├── ADR-001-vector-db-selection.md
    │   ├── ADR-002-kafka-read-path-exclusion.md
    │   └── ADR-003-heuristic-importance-scoring.md
    └── runbooks/
```

---

## 3. Technical Stack & Constraints

> **AGENT RULE:** Do not deviate from these versions. If a dependency conflict arises, document it in `docs/adr/` and resolve it — do not silently upgrade or downgrade.

### 3.1 Core Stack

| Layer | Technology | Version | Notes |
|-------|-----------|---------|-------|
| Language | Java | **21 LTS** | Use virtual threads (Project Loom) for I/O-bound services |
| Framework | Spring Boot | **3.3.x** | Do not use Spring Boot 2.x patterns |
| Build | Gradle | **8.7** | Kotlin DSL (`build.gradle.kts`) |
| API — Sync | Spring WebMVC + gRPC (grpc-spring-boot-starter) | **2.15.0** | gRPC for inter-service retrieval |
| API — Async | Apache Kafka | **3.6.x** | Confluent Schema Registry for Avro |
| ORM | Spring Data JPA + Hibernate | **6.4.x** | Named queries only, no raw JPQL strings |
| Database | PostgreSQL | **16** | Row-Level Security enforced at DB level |
| Vector DB | Weaviate | **1.24.x** | Multi-tenant class per tenant |
| Search | OpenSearch | **2.12.x** | BM25 sparse retrieval |
| Cache | Redis | **7.2** | Lettuce client, read-path only |
| Batch | Spring Batch | **5.1.x** | Reindex pipeline |
| LLM Orchestration | LangChain4j | **0.31.x** | HyDE + summarization only |
| Embedding Model | `text-embedding-3-small` (OpenAI) | dim=1536 | Switchable via `ModelVersionRegistry` |
| Observability | Micrometer + Prometheus + Grafana | latest stable | Custom memory hit-rate meter |
| Tracing | OpenTelemetry + Jaeger | **1.36.x** | Trace every retrieval pipeline stage |
| Containerisation | Docker + Kubernetes | Docker 25, K8s 1.29 | |
| CI/CD | GitHub Actions | — | |
| Testing | JUnit 5 + Mockito 5 + Testcontainers | **3.3.x** | |
| Code Quality | Checkstyle + SpotBugs + JaCoCo | — | ≥80% coverage gate |

### 3.2 Hard Constraints

- **No Lombok.** Use Java Records for immutable DTOs and standard constructors for entities.
- **No raw SQL strings** in Java code. Use named queries or Spring Data method names.
- **No `@Autowired` on fields.** Constructor injection only.
- **No synchronous Kafka on the read path.** Retrieval = gRPC. Writes and analytics = Kafka.
- **No LLM calls for importance scoring.** Heuristic model only in `ImportanceScoringService`.
- **All inter-service secrets** via Kubernetes Secrets or Vault — never hardcoded or in `application.yml`.

---

## 4. Agent Persona & Implementation Standards

### 4.1 Persona
You are a **Lead Software Engineer** with 15 years of experience in distributed systems, JVM performance, and enterprise data platforms. You write code that a team of five engineers will maintain for five years. Every decision is intentional. Every class has a single responsibility. Every failure mode is handled explicitly.

### 4.2 Code Standards

**Documentation**
- Every `public` class: Javadoc with `@author agent`, `@since 1.0.0`, and a one-paragraph description of responsibility.
- Every `public` method: Javadoc with `@param`, `@return`, `@throws`.
- Every non-obvious block of logic: inline comment explaining *why*, not *what*.

**Error Handling**
- Never swallow exceptions (`catch (Exception e) {}`).
- All service-layer exceptions must be typed (extend `AiMemoryException` base class).
- All REST endpoints return structured `ErrorResponse` records — never raw strings.
- All Kafka consumers use a dead-letter topic (`<topic>.DLT`) with retry policy: 3 retries, exponential backoff (1s, 2s, 4s).

**Modularity**
- One class = one responsibility. If a class exceeds 250 lines, split it.
- No circular dependencies between packages. Use the layered pattern strictly: `api` → `service` → `repository` → `domain`.
- Shared types live in `shared-lib` only — no copy-pasting DTOs between services.

**Security**
- All `tenantId` values must be sourced from the JWT, not from the request body.
- Row-Level Security must be enabled in PostgreSQL and verified in integration tests by attempting cross-tenant access.
- Every endpoint must be annotated with `@PreAuthorize`.

---

## 5. Reference Code Snippets

> **AGENT RULE:** Use these patterns exactly. They encode the standards for this project. Any deviation requires an ADR.

### 5.1 Core Memory Entity

```java
/**
 * Core domain entity representing a single memory record.
 * Implements versioning, provenance tracking, and soft-delete lifecycle.
 *
 * @author agent
 * @since 1.0.0
 */
@Entity
@Table(name = "memories")
@EntityListeners(AuditingEntityListener.class)
public class Memory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID memoryId;

    @Column(nullable = false)
    private UUID tenantId;                  // From JWT — never from request body

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryType memoryType;          // EPISODIC | SEMANTIC

    // Provenance — resolves gap: where did this memory come from?
    @Column(nullable = false)
    private UUID sourceConversationId;

    @Column(nullable = false)
    private UUID sourceSessionId;

    // Versioning — resolves gap: memory evolution tracking
    @Column(nullable = false)
    private int version = 1;

    @Column
    private UUID replacesMemoryId;          // Null for original records

    // Embedding coupling — resolves gap: reindex safety
    @Column(nullable = false)
    private String embeddingModelVersion;   // e.g. "text-embedding-3-small-v1"

    @Column(nullable = false)
    private int embeddingDimension;

    // Importance scoring — heuristic-computed, never LLM-assigned
    @Column(nullable = false)
    private double importanceScore;

    @Column(nullable = false)
    private long retrievalCount = 0;

    @Column(nullable = false)
    private Instant lastRetrievedAt;

    // Soft-delete lifecycle — resolves gap: GDPR + pruning safety
    @Column(nullable = false)
    private boolean softDeleted = false;

    @Column
    private Instant softDeletedAt;

    @Column
    private Instant hardDeleteEligibleAt;   // softDeletedAt + 30 days

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Standard constructors, getters — no Lombok
}
```

### 5.2 Retrieval Pipeline (HyDE + Hybrid + Re-rank)

```java
/**
 * Orchestrates the full retrieval pipeline for a user query.
 * Pipeline: HyDE rewrite → parallel hybrid search → cross-encoder re-rank → budget allocation.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class RetrievalPipelineService {

    private static final Logger log = LoggerFactory.getLogger(RetrievalPipelineService.class);
    private static final int INITIAL_CANDIDATE_LIMIT = 50;
    private static final int FINAL_CONTEXT_LIMIT = 10;

    private final QueryRewriterService queryRewriter;
    private final HybridSearchService hybridSearch;
    private final ReRankingService reRanker;
    private final ContextBudgetAllocator budgetAllocator;

    public RetrievalPipelineService(
            QueryRewriterService queryRewriter,
            HybridSearchService hybridSearch,
            ReRankingService reRanker,
            ContextBudgetAllocator budgetAllocator) {
        this.queryRewriter = queryRewriter;
        this.hybridSearch = hybridSearch;
        this.reRanker = reRanker;
        this.budgetAllocator = budgetAllocator;
    }

    /**
     * Executes the full retrieval pipeline for a given query within a tenant context.
     *
     * @param query     the raw user query string
     * @param tenantId  the tenant identifier, sourced from JWT
     * @param userId    the user identifier
     * @param tokenBudget maximum number of tokens available for context injection
     * @return ordered list of memories to inject into the LLM prompt
     * @throws RetrievalException if any pipeline stage fails unrecoverably
     */
    public List<RankedMemory> retrieve(
            String query, UUID tenantId, UUID userId, int tokenBudget) {

        // Stage 1: HyDE — generate a hypothetical document to improve embedding quality
        String hydeDocument = queryRewriter.generateHypotheticalDocument(query);
        log.debug("HyDE document generated for query hash={}", query.hashCode());

        // Stage 2: Parallel hybrid search — dense + sparse simultaneously
        List<MemoryCandidate> candidates = hybridSearch.search(
                hydeDocument, query, tenantId, userId, INITIAL_CANDIDATE_LIMIT);

        if (candidates.isEmpty()) {
            log.info("No candidates found for tenantId={} userId={}", tenantId, userId);
            return List.of();
        }

        // Stage 3: Cross-encoder re-ranking on initial candidates
        List<RankedMemory> reRanked = reRanker.reRank(query, candidates);

        // Stage 4: Token budget allocation — trim to fit context window
        return budgetAllocator.allocate(reRanked, tokenBudget, FINAL_CONTEXT_LIMIT);
    }
}
```

### 5.3 Importance Scoring (Heuristic — No LLM)

```java
/**
 * Computes memory importance using a weighted heuristic model.
 * Deliberately avoids LLM calls to keep scoring latency under 5ms.
 * Formula: score = (recencyWeight * recency) + (frequencyWeight * frequency)
 *                  + (salienceWeight * salience)
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ImportanceScoringService {

    private static final double RECENCY_WEIGHT   = 0.40;
    private static final double FREQUENCY_WEIGHT = 0.35;
    private static final double SALIENCE_WEIGHT  = 0.25;
    private static final double DECAY_HALF_LIFE_DAYS = 30.0;

    private final EntitySalienceScorer salienceScorer;

    public ImportanceScoringService(EntitySalienceScorer salienceScorer) {
        this.salienceScorer = salienceScorer;
    }

    /**
     * Scores a memory's importance as a double in the range [0.0, 1.0].
     *
     * @param memory the memory entity to score
     * @return importance score clamped to [0.0, 1.0]
     */
    public double score(Memory memory) {
        double recency   = computeRecencyScore(memory.getCreatedAt());
        double frequency = computeFrequencyScore(memory.getRetrievalCount());
        double salience  = salienceScorer.score(memory.getContent());

        double raw = (RECENCY_WEIGHT * recency)
                   + (FREQUENCY_WEIGHT * frequency)
                   + (SALIENCE_WEIGHT * salience);

        return Math.max(0.0, Math.min(1.0, raw));
    }

    private double computeRecencyScore(Instant createdAt) {
        long ageInDays = ChronoUnit.DAYS.between(createdAt, Instant.now());
        // Exponential decay: score halves every DECAY_HALF_LIFE_DAYS days
        return Math.pow(0.5, ageInDays / DECAY_HALF_LIFE_DAYS);
    }

    private double computeFrequencyScore(long retrievalCount) {
        // Logarithmic scale: diminishing returns after ~20 retrievals
        return Math.min(1.0, Math.log1p(retrievalCount) / Math.log1p(20));
    }
}
```

### 5.4 Standard REST Controller Pattern

```java
/**
 * REST controller for memory CRUD operations.
 * All tenantId values are extracted from SecurityContext — never from the request body.
 *
 * @author agent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/memories")
@Validated
public class MemoryController {

    private final MemoryWriteService writeService;

    public MemoryController(MemoryWriteService writeService) {
        this.writeService = writeService;
    }

    /**
     * Creates a new memory record for the authenticated user.
     *
     * @param request the memory creation payload
     * @param principal the authenticated principal carrying tenant context
     * @return 201 Created with the persisted memory response
     */
    @PostMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('USER')")
    public ResponseEntity<MemoryResponse> createMemory(
            @Valid @RequestBody MemoryCreateRequest request,
            @AuthenticationPrincipal TenantPrincipal principal) {

        MemoryResponse response = writeService.create(request, principal.tenantId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### 5.5 Standard Error Response

```java
/**
 * Standardised error envelope returned by all REST endpoints on failure.
 *
 * @param status    HTTP status code
 * @param errorCode machine-readable error code (e.g. "MEMORY_NOT_FOUND")
 * @param message   human-readable description
 * @param traceId   OpenTelemetry trace ID for log correlation
 * @param timestamp UTC timestamp of the error
 */
public record ErrorResponse(
    int status,
    String errorCode,
    String message,
    String traceId,
    Instant timestamp
) {
    public static ErrorResponse of(int status, String errorCode, String message, String traceId) {
        return new ErrorResponse(status, errorCode, message, traceId, Instant.now());
    }
}
```

### 5.6 Kafka Consumer with DLT

```java
/**
 * Consumes embedding-requested events and delegates to the generation service.
 * Implements 3-retry exponential backoff with dead-letter routing on exhaustion.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class EmbeddingRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingRequestConsumer.class);

    private final EmbeddingGenerationService generationService;

    public EmbeddingRequestConsumer(EmbeddingGenerationService generationService) {
        this.generationService = generationService;
    }

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "embedding.requested", groupId = "embedding-service")
    public void consume(EmbeddingRequestedEvent event) {
        log.info("Processing embedding request for memoryId={}", event.memoryId());
        try {
            generationService.generateAndStore(event);
        } catch (EmbeddingGenerationException e) {
            log.error("Embedding generation failed for memoryId={}", event.memoryId(), e);
            throw e; // Re-throw so @RetryableTopic handles retry/DLT routing
        }
    }

    @DltHandler
    public void handleDlt(EmbeddingRequestedEvent event) {
        log.error("DEAD LETTER: Embedding permanently failed for memoryId={}", event.memoryId());
        // Alert via Prometheus metric — do not silently discard
    }
}
```

### 5.7 PostgreSQL Migration (Flyway)

```sql
-- V1__create_memories_table.sql
CREATE TABLE memories (
    memory_id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL,
    user_id                 UUID NOT NULL,
    content                 TEXT NOT NULL,
    memory_type             VARCHAR(20) NOT NULL CHECK (memory_type IN ('EPISODIC', 'SEMANTIC')),
    source_conversation_id  UUID NOT NULL,
    source_session_id       UUID NOT NULL,
    version                 INT NOT NULL DEFAULT 1,
    replaces_memory_id      UUID REFERENCES memories(memory_id),
    embedding_model_version VARCHAR(100) NOT NULL,
    embedding_dimension     INT NOT NULL,
    importance_score        DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    retrieval_count         BIGINT NOT NULL DEFAULT 0,
    last_retrieved_at       TIMESTAMPTZ,
    soft_deleted            BOOLEAN NOT NULL DEFAULT FALSE,
    soft_deleted_at         TIMESTAMPTZ,
    hard_delete_eligible_at TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Row-Level Security: tenants can only see their own rows
ALTER TABLE memories ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON memories
    USING (tenant_id = current_setting('app.current_tenant_id')::UUID);

-- Performance indexes
CREATE INDEX idx_memories_tenant_user ON memories (tenant_id, user_id) WHERE NOT soft_deleted;
CREATE INDEX idx_memories_embedding_model ON memories (embedding_model_version);
CREATE INDEX idx_memories_importance ON memories (tenant_id, importance_score DESC) WHERE NOT soft_deleted;
```

## Appendix A — Kafka Topic Registry

| Topic | Producer | Consumer(s) | Schema | Retention |
|-------|----------|-------------|--------|-----------|
| `memory.ingested` | Memory Svc | Embedding Svc, Ranking Engine | Avro | 7 days |
| `memory.versioned` | Memory Svc | Ranking Engine, Cache Invalidator | Avro | 7 days |
| `memory.retrieved` | Retrieval Svc | Ranking Engine | Avro | 3 days |
| `memory.pruned` | Pruning Engine | Compliance Svc, Audit | Avro | 30 days |
| `embedding.requested` | Memory Svc | Embedding Svc | Avro | 3 days |
| `embedding.completed` | Embedding Svc | Memory Svc (update model version) | Avro | 3 days |
| `reindex.triggered` | ModelVersionRegistry | Reindex Worker | Avro | 30 days |
| `agent.conversation` | Agent Svc | Memory Extraction Worker | Avro | 7 days |
| `audit.event` | All Services | Compliance Svc | Avro | 365 days |
| `*.DLT` | Kafka (auto) | Ops Alerting | Raw | 7 days |

---

## Appendix B — Environment Variables (Required per Service)

```
# Shared
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/aimemory
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
REDIS_HOST=redis
REDIS_PORT=6379

# Auth Service
JWT_SECRET=<min 256-bit secret>
JWT_EXPIRY_SECONDS=3600

# Embedding Service
OPENAI_API_KEY=<secret>
EMBEDDING_MODEL_NAME=text-embedding-3-small
COHERE_API_KEY=<secret>        # for re-ranking

# Retrieval Service
WEAVIATE_HOST=weaviate
WEAVIATE_PORT=8080
OPENSEARCH_HOST=opensearch
OPENSEARCH_PORT=9200

# Ranking Engine
PRUNING_IMPORTANCE_THRESHOLD=0.15
PRUNING_INACTIVITY_DAYS=90
RECENCY_DECAY_HALF_LIFE_DAYS=30
```
