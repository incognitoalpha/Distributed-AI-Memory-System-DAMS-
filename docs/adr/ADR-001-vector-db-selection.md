# ADR-001: Vector Database Selection

## Status
Accepted

## Context
We need to select a vector database for storing and searching dense embeddings. The system must support:
- Multi-tenant isolation
- High-dimensional vectors (1536+ dimensions)
- Approximate nearest neighbor search
- Horizontal scaling

## Decision
**Weaviate 1.24.x** will be used as the primary vector database.

## Rationale

### Criteria Evaluated
| Criterion | Weaviate | Pinecone | Milvus | Qdrant |
|-----------|----------|----------|--------|--------|
| Multi-tenant support | ✅ Native | ✅ Native | ✅ Native | ✅ Native |
| Open source | ✅ | ❌ | ✅ | ✅ |
| Cloud-native | ✅ | ✅ | ✅ | ✅ |
| GraphQL API | ✅ | ❌ | ❌ | ❌ |
| BM25 hybrid search | ✅ Built-in | ❌ | ✅ | ❌ |

### Why Weaviate
1. **Native multi-tenancy** - Per-tenant class isolation built-in
2. **Hybrid search** - Built-in BM25 + vector search
3. **GraphQL** - Rich filtering and aggregation
4. **Active community** - Strong enterprise adoption
5. **Kubernetes-ready** - Helm charts available

## Consequences

### Positive
- Unified API for vector + keyword search
- No separate BM25 engine required
- Rich filtering at query time

### Negative
- Single-node performance limited (requires clustering for scale)
- Cloud managed version pricing

## References
- Weaviate Documentation: https://weaviate.io/documentation
- PRD Section 2.1: System Architecture