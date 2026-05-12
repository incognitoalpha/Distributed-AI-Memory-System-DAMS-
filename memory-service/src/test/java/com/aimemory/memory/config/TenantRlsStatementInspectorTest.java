package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantRlsStatementInspectorTest {

    private final TenantRlsStatementInspector inspector = new TenantRlsStatementInspector();

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void inspect_addsTenantIdWhenContextIsSet() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantContext.set(tenantId, userId);
        String sql = "SELECT * FROM memories";

        // Act
        String result = inspector.inspect(sql);

        // Assert
        assertThat(result).contains("SET LOCAL app.current_tenant_id = '" + tenantId + "'");
        assertThat(result).contains(sql);
    }

    @Test
    void inspect_returnsOriginalSqlWhenContextIsNotSet() {
        // Arrange
        String sql = "SELECT * FROM memories";

        // Act
        String result = inspector.inspect(sql);

        // Assert
        assertThat(result).isEqualTo(sql);
    }
}
