-- V6__create_embedding_model_versions_table.sql
-- Tracks embedding model versions for reindexing

CREATE TABLE embedding_model_versions (
    model_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_name        VARCHAR(100) NOT NULL UNIQUE,
    model_version     VARCHAR(50) NOT NULL,
    dimension         INT NOT NULL,
    is_active         BOOLEAN NOT NULL DEFAULT FALSE,
    activated_at      TIMESTAMPTZ,
    deactivated_at    TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for active model lookup
CREATE INDEX idx_embedding_model_active ON embedding_model_versions (is_active) WHERE is_active;

-- Insert default model
INSERT INTO embedding_model_versions (model_name, model_version, dimension, is_active, activated_at)
VALUES ('text-embedding-3-small', 'v1', 1536, true, NOW());