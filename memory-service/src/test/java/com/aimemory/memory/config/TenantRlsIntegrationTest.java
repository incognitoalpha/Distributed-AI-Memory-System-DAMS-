package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class TenantRlsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private UUID tenantA = UUID.randomUUID();
    private UUID tenantB = UUID.randomUUID();
    private UUID userA = UUID.randomUUID();
    private UUID userB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Bypass RLS for cleanup and ensure it's forced for testing
        jdbcTemplate.execute("ALTER TABLE memories DISABLE ROW LEVEL SECURITY");
        jdbcTemplate.execute("DELETE FROM memories");
        jdbcTemplate.execute("ALTER TABLE memories ENABLE ROW LEVEL SECURITY");
        jdbcTemplate.execute("ALTER TABLE memories FORCE ROW LEVEL SECURITY");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void rls_filtersDataByTenant() {
        jdbcTemplate.execute("ALTER TABLE memories DISABLE ROW LEVEL SECURITY");
        insertMemory(tenantA, userA, "Memory for Tenant A");
        insertMemory(tenantB, userB, "Memory for Tenant B");
        jdbcTemplate.execute("ALTER TABLE memories ENABLE ROW LEVEL SECURITY");
        jdbcTemplate.execute("ALTER TABLE memories FORCE ROW LEVEL SECURITY");

        // 1. Switch to Tenant A and verify ONLY memoryA is visible through RLS.
        TenantContext.set(tenantA, userA);
        jdbcTemplate.execute("SET app.current_tenant_id = '" + tenantA + "'");
        List<String> allMemories = jdbcTemplate.queryForList("SELECT content FROM memories ORDER BY content", String.class);
        String dbTenantId = jdbcTemplate.queryForObject("SELECT current_setting('app.current_tenant_id', true)", String.class);
        
        assertThat(allMemories)
            .as("Expected 1 memory for tenant A, but found " + allMemories.size() + 
                ". DB app.current_tenant_id=" + dbTenantId + ". Items: " + allMemories)
            .hasSize(1);
        assertThat(allMemories.get(0)).isEqualTo("Memory for Tenant A");

        // 2. Switch to Tenant B and verify ONLY memoryB is visible through RLS.
        TenantContext.set(tenantB, userB);
        jdbcTemplate.execute("SET app.current_tenant_id = '" + tenantB + "'");
        allMemories = jdbcTemplate.queryForList("SELECT content FROM memories ORDER BY content", String.class);
        dbTenantId = jdbcTemplate.queryForObject("SELECT current_setting('app.current_tenant_id', true)", String.class);
        
        assertThat(allMemories)
            .as("Expected 1 memory for tenant B, but found " + allMemories.size() + 
                ". DB app.current_tenant_id=" + dbTenantId + ". Items: " + allMemories)
            .hasSize(1);
        assertThat(allMemories.get(0)).isEqualTo("Memory for Tenant B");
    }

    private void insertMemory(UUID tenantId, UUID userId, String content) {
        jdbcTemplate.update("""
                INSERT INTO memories (
                    tenant_id,
                    user_id,
                    content,
                    memory_type,
                    source_conversation_id,
                    source_session_id,
                    version,
                    embedding_model_version,
                    embedding_dimension,
                    importance_score,
                    retrieval_count,
                    last_retrieved_at,
                    soft_deleted
                )
                VALUES (?, ?, ?, 'EPISODIC', ?, ?, 1, 'text-embedding-3-small', 1536, 0.8, 0, NOW(), false)
                """,
                tenantId,
                userId,
                content,
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }
}
