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
            
            // Check if it's a DML statement
            String cleanSql = sql.replaceAll("/\\*.*?\\*/", "").trim().toLowerCase();
            
            if (cleanSql.startsWith("insert") || cleanSql.startsWith("update") || cleanSql.startsWith("delete")) {
                // Prepend SET LOCAL and append SELECT 1. 
                // This ensures JDBC returns the count for the actual DML statement when executed as a batch.
                return String.format("SET LOCAL app.current_tenant_id = '%s'; %s",
                        tenantId.toString(), sql);
            }
            
            // PostgreSQL session variable prefix
            return String.format("SET LOCAL app.current_tenant_id = '%s'; %s", 
                    tenantId.toString(), sql);
        }
        
        log.warn("Executing SQL without tenant context! This may fail if RLS is enabled on the table.");
        return sql;
    }
}
