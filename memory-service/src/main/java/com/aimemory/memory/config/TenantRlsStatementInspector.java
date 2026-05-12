package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Hibernate statement inspector to enforce Row-Level Security (RLS) in PostgreSQL.
 * Prefixes SQL statements with a command to set the session-local tenant ID variable.
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
            
            // If it's a DML statement (INSERT/UPDATE/DELETE), we use a DO block
            // to execute the SET command and the statement together.
            // PostgreSQL DO blocks do not return row counts to JDBC, which is why 
            // Hibernate throws StaleStateException.
            // The most compatible way to set session variables in PostgreSQL with Hibernate
            // is actually to use a Connection proxy or a dedicated initialization SQL,
            // but since we need it dynamic per-request, we prepend it.
            
            return String.format("SET LOCAL app.current_tenant_id = '%s'; %s", 
                    tenantId.toString(), sql);
        }
        
        log.warn("Executing SQL without tenant context! This may fail if RLS is enabled on the table.");
        return sql;
    }
}