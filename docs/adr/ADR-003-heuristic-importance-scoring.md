# ADR-003: Heuristic Importance Scoring (No LLM)

## Status
Accepted

## Context
Memory importance scoring must be fast (< 5ms) to avoid impacting retrieval latency. Using LLM for scoring would add 500ms+ per memory.

## Decision
**Use weighted heuristic model: recency + frequency + entity salience**

Formula: `score = (0.40 × recency) + (0.35 × frequency) + (0.25 × salience)`

## Rationale

### Options Considered

| Approach | Latency | Accuracy | Cost |
|----------|---------|-----------|------|
| LLM scoring | 500ms+ | High | High |
| Transformer embedding | 50ms | High | Medium |
| Heuristic (chosen) | < 1ms | Medium | None |

### Why Heuristic
1. **Speed** - < 1ms vs 500ms+ for LLM
2. **Deterministic** - Same input = same output, easy to test
3. **No API costs** - Free to compute
4. **Sufficient accuracy** - Good enough for memory prioritization

### Components

#### 1. Recency Decay (40%)
- Exponential decay: `score = 0.5^(age/halfLife)`
- Half-life: 30 days default
- Formula: newer memories score higher

#### 2. Retrieval Frequency (35%)
- Logarithmic scale: `score = log(1+retrievals) / log(1+cap)`
- Cap: 20 retrievals max
- Diminishing returns after 20 accesses

#### 3. Entity Salience (25%)
- Keyword density in content
- Named entity regex patterns
- No LLM required

## Consequences

### Positive
- Sub-millisecond computation
- No external API dependencies
- Deterministic, testable
- Good enough for ranking

### Negative
- Less nuanced than LLM scoring
- Cannot understand semantic meaning
- May miss context-dependent importance

## References
- PRD Section 5.3: ImportanceScoringService reference implementation
- ranking-engine/src/main/java/.../ImportanceScoringService.java