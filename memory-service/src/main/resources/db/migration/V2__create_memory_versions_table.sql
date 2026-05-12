-- V2__create_memory_versions_table.sql
-- Tracks every mutation of a memory record for full version history

CREATE TABLE memory_versions (
    version_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    memory_id          UUID NOT NULL REFERENCES memories(memory_id) ON DELETE CASCADE,
    version            INT NOT NULL,
    content            TEXT NOT NULL,
    change_reason      TEXT,
    modified_by        UUID NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for efficient version lookups
CREATE INDEX idx_memory_versions_memory_id ON memory_versions (memory_id, version DESC);

-- Index for audit queries
CREATE INDEX idx_memory_versions_created_at ON memory_versions (created_at);