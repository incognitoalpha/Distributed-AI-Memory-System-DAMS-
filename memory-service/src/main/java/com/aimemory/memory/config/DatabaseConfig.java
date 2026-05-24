package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Configuration to enhance database connectivity with RLS support.
 * Wraps the default DataSource to automatically set the PostgreSQL session 
 * variable 'app.current_tenant_id' whenever a connection is retrieved, 
 * provided a tenant context is set.
 * 
 * @author agent
 * @since 1.0.0
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        DataSource baseDataSource = properties.initializeDataSourceBuilder().build();
        return new TenantAwareDataSource(baseDataSource);
    }

    /**
     * DataSource wrapper that executes 'SET LOCAL app.current_tenant_id' 
     * on every connection retrieval if TenantContext is available.
     */
    private static class TenantAwareDataSource extends DelegatingDataSource {
        
        public TenantAwareDataSource(DataSource delegate) {
            super(delegate);
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = super.getConnection();
            setTenantContext(connection);
            return connection;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            Connection connection = super.getConnection(username, password);
            setTenantContext(connection);
            return connection;
        }

        private void setTenantContext(Connection connection) throws SQLException {
            if (TenantContext.isSet()) {
                String tenantId = TenantContext.getTenantId().toString();
                try (Statement stmt = connection.createStatement()) {
                    // Use SET LOCAL so it's scoped to the current transaction
                    stmt.execute("SET LOCAL app.current_tenant_id = '" + tenantId + "'");
                }
            }
        }
    }
}
