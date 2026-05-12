-- V4__create_audit_log_table.sql
-- Immutable audit log for all memory operations (GDPR compliance)

CREATE TABLE audit_log (
    audit_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    user_id         UUID NOT NULL,
    event_type      VARCHAR(50) NOT NULL,
    resource_type   VARCHAR(50) NOT NULL,
    resource_id     UUID,
    details         JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(255),
    timestamp       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for audit queries by tenant
CREATE INDEX idx_audit_log_tenant_id ON audit_log (tenant_id);

-- Index for audit queries by user
CREATE INDEX idx_audit_log_user_id ON audit_log (user_id);

-- Index for audit queries by timestamp
CREATE INDEX idx_audit_log_timestamp ON audit_log (timestamp DESC);

-- Index for audit queries by event type
CREATE INDEX idx_audit_log_event_type ON audit_log (event_type);