package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native Hibernate Statement Inspector to enforce Row-Level Security (RLS).
 * Intercepts SQL strings before they are sent to PostgreSQL and prepends
 * the 'SET LOCAL app.current_tenant_id' command.
 * 
 * @author agent
 * @since 1.0.0
 */
public class TenantRlsStatementInspector implements StatementInspector {

    private static final Logger log = LoggerFactory.getLogger(TenantRlsStatementInspector.class);

    @Override
    public String inspect(String sql) {
        if (TenantContext.isSet()) {
            String tenantId = TenantContext.getTenantId().toString();
            // Prepend the SET LOCAL command. 
            // We use SET LOCAL so it's scoped to the current transaction.
            String modifiedSql = "SET LOCAL app.current_tenant_id = '" + tenantId + "'; " + sql;
            log.trace("RLS SQL inspection: Prepending tenant context {}", tenantId);
            return modifiedSql;
        }
        
        // If no tenant context is set, we return the SQL unchanged.
        // Tables with FORCE ROW LEVEL SECURITY will automatically block the query
        // if app.current_tenant_id is not set.
        return sql;
    }
}
