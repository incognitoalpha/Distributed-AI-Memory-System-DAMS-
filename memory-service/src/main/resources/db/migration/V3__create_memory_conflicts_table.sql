-- V3__create_memory_conflicts_table.sql
-- Records detected conflicts when newer memories contradict older ones

CREATE TABLE memory_conflicts (
    conflict_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL,
    old_memory_id         UUID NOT NULL REFERENCES memories(memory_id) ON DELETE CASCADE,
    new_memory_id         UUID NOT NULL REFERENCES memories(memory_id) ON DELETE CASCADE,
    similarity_score      DOUBLE PRECISION NOT NULL,
    resolution_strategy   VARCHAR(20) NOT NULL CHECK (resolution_strategy IN ('LATEST_WINS', 'MERGE')),
    resolution_details    TEXT,
    resolved              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for conflict lookups by memory
CREATE INDEX idx_memory_conflicts_old_memory ON memory_conflicts (old_memory_id);
CREATE INDEX idx_memory_conflicts_new_memory ON memory_conflicts (new_memory_id);

-- Index for unresolved conflicts
CREATE INDEX idx_memory_conflicts_unresolved ON memory_conflicts (tenant_id, resolved) WHERE NOT resolved;