package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Hibernate statement inspector to enforce Row-Level Security (RLS) in PostgreSQL.
 * Prefixes every SQL statement with a command to set the session-local tenant ID variable.
 * 
 * @author agent
 * @since 1.0.0
 */
public class TenantRlsStatementInspector implements StatementInspector {

    private static final Logger log = LoggerFactory.getLogger(TenantRlsStatementInspector.class);

    @Override
    public String inspect(String sql) {
        if (TenantContext.isSet()) {
            UUID tenantId = TenantContext.getTenantId();
            log.trace("Injecting tenant context into SQL: tenantId={}", tenantId);
            
            // Check if it's a DML statement, ignoring potential comments and whitespace
            // Regex matches optional whitespace/comments followed by INSERT, UPDATE, or DELETE
            String cleanSql = sql.replaceAll("/\\*.*?\\*/", "").trim().toLowerCase();
            
            if (cleanSql.startsWith("insert") || cleanSql.startsWith("update") || cleanSql.startsWith("delete")) {
                // Use a CTE for DML to ensure JDBC executeUpdate returns the correct row count
                // avoiding Hibernate StaleStateException.
                return String.format("WITH rls_ctx AS (SELECT set_config('app.current_tenant_id', '%s', true)) %s",
                        tenantId.toString(), sql);
            }
            
            // PostgreSQL session variable prefix
            // We use 'SET LOCAL' so it only applies to the current transaction
            return String.format("SET LOCAL app.current_tenant_id = '%s'; %s", 
                    tenantId.toString(), sql);
        }
        
        log.warn("Executing SQL without tenant context! This may fail if RLS is enabled on the table.");
        return sql;
    }
}
