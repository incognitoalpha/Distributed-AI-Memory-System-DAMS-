-- V5__create_pruning_queue_table.sql
-- Staging area for soft-deleted memories pending hard-delete or summarization

CREATE TABLE pruning_queue (
    queue_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    memory_id           UUID NOT NULL REFERENCES memories(memory_id) ON DELETE CASCADE,
    tenant_id           UUID NOT NULL,
    user_id             UUID NOT NULL,
    pruning_type        VARCHAR(20) NOT NULL CHECK (pruning_type IN ('HARD_DELETE', 'SUMMARIZE')),
    importance_score   DOUBLE PRECISION,
    reason              VARCHAR(255),
    scheduled_for       TIMESTAMPTZ NOT NULL,
    processed           BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for scheduling
CREATE INDEX idx_pruning_queue_scheduled ON pruning_queue (scheduled_for) WHERE NOT processed;

-- Index by tenant
CREATE INDEX idx_pruning_queue_tenant ON pruning_queue (tenant_id);