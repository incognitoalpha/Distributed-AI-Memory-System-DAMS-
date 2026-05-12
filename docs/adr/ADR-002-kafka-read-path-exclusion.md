# ADR-002: Kafka Exclusion from Read Hot Path

## Status
Accepted

## Context
User-facing retrieval must meet P99 latency < 400ms. The system uses Kafka for async events, but we must ensure Kafka is never on the synchronous read path.

## Decision
**Retrieval Service uses direct gRPC/REST calls only. Kafka is exclusively for write and analytics paths.**

## Rationale

### Latency Requirements
| Path | Target | Kafka Overhead |
|------|--------|----------------|
| Read (cached) | < 80ms | N/A |
| Read (uncached) | < 400ms | N/A |
| Write | Async | 50-200ms |

### Why Not Kafka for Reads
1. **Serialization/deserialization** - JSON/Avro overhead per request
2. **Broker round-trip** - Network hop to broker, then to consumer
3. **Consumer group coordination** - Partition rebalancing adds latency
4. **No request/response pattern** - Requires separate callback infrastructure

### Architecture
```
User Request → Gateway → Retrieval Service → Weaviate/OpenSearch → Response
                                      ↓ (async)
                                Kafka (events only)
```

## Consequences

### Positive
- Predictable, low-latency reads
- Direct service-to-service communication
- Easier debugging (no message broker in path)

### Negative
- Cannot use Kafka consumer groups for load balancing reads
- Must implement circuit breaker in Retrieval Service

## References
- PRD Section 2.1: "Critical rule: Kafka is never on the synchronous retrieval hot path"
- Kafka Topics: memory.ingested, memory.retrieved (events only, not reads)