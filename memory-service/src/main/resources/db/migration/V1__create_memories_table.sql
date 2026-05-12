-- V1__create_memories_table.sql
-- Core memories table with Row-Level Security

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

-- Performance indexes
CREATE INDEX idx_memories_tenant_user ON memories (tenant_id, user_id) WHERE NOT soft_deleted;
CREATE INDEX idx_memories_embedding_model ON memories (embedding_model_version);
CREATE INDEX idx_memories_importance ON memories (tenant_id, importance_score DESC) WHERE NOT soft_deleted;
CREATE INDEX idx_memories_conversation ON memories (source_conversation_id, tenant_id) WHERE NOT soft_deleted;
CREATE INDEX idx_memories_soft_deleted ON memories (soft_deleted) WHERE NOT soft_deleted;

-- Row-Level Security
ALTER TABLE memories ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON memories
    USING (tenant_id = current_setting('app.current_tenant_id', true)::UUID);

-- Enable pgcrypto extension if not already available
CREATE EXTENSION IF NOT EXISTS pgcrypto;