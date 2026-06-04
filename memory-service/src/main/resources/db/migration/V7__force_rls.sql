-- V7__force_rls.sql
-- Enable and enforce Row-Level Security for all tenant-data tables

-- memories (already has policy from V1, just force it for owner)
ALTER TABLE memories FORCE ROW LEVEL SECURITY;

-- memory_versions (inherit from memories via join)
ALTER TABLE memory_versions ENABLE ROW LEVEL SECURITY;
ALTER TABLE memory_versions FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_versions ON memory_versions
    USING (
        EXISTS (
            SELECT 1
            FROM memories
            WHERE memories.memory_id = memory_versions.memory_id
              AND memories.tenant_id = current_setting('app.current_tenant_id', true)::UUID
        )
    );

-- memory_conflicts
ALTER TABLE memory_conflicts ENABLE ROW LEVEL SECURITY;
ALTER TABLE memory_conflicts FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_conflicts ON memory_conflicts
    USING (tenant_id = current_setting('app.current_tenant_id', true)::UUID);

-- audit_log
ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_audit ON audit_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::UUID);

-- pruning_queue
ALTER TABLE pruning_queue ENABLE ROW LEVEL SECURITY;
ALTER TABLE pruning_queue FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_pruning ON pruning_queue
    USING (tenant_id = current_setting('app.current_tenant_id', true)::UUID);
