package com.aimemory.memory.config;

import com.aimemory.memory.domain.Memory;
import com.aimemory.memory.domain.enums.MemoryType;
import com.aimemory.memory.repository.MemoryRepository;
import com.aimemory.shared.domain.TenantContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
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
    private MemoryRepository memoryRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private UUID tenantA = UUID.randomUUID();
    private UUID tenantB = UUID.randomUUID();
    private UUID userA = UUID.randomUUID();
    private UUID userB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Bypass RLS for cleanup and ensure it's forced for testing
        jdbcTemplate.execute("ALTER TABLE memories DISABLE ROW LEVEL SECURITY");
        memoryRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE memories ENABLE ROW LEVEL SECURITY");
        jdbcTemplate.execute("ALTER TABLE memories FORCE ROW LEVEL SECURITY");
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @Transactional
    void rls_filtersDataByTenant() {
        // 1. Create data for Tenant A using JdbcTemplate to bypass Hibernate update count checks
        TenantContext.set(tenantA, userA);
        String sql = "INSERT INTO memories (tenant_id, user_id, content, memory_type, source_conversation_id, source_session_id, embedding_model_version, embedding_dimension, importance_score, retrieval_count, last_retrieved_at, version, soft_deleted, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql, tenantA, userA, "Memory for Tenant A", "EPISODIC", UUID.randomUUID(), UUID.randomUUID(), "text-embedding-3-small", 1536, 0.8, 0, java.sql.Timestamp.from(Instant.now()), 1, false, java.sql.Timestamp.from(Instant.now()), java.sql.Timestamp.from(Instant.now()));

        // 2. Create data for Tenant B using JdbcTemplate
        TenantContext.set(tenantB, userB);
        jdbcTemplate.update(sql, tenantB, userB, "Memory for Tenant B", "EPISODIC", UUID.randomUUID(), UUID.randomUUID(), "text-embedding-3-small", 1536, 0.8, 0, java.sql.Timestamp.from(Instant.now()), 1, false, java.sql.Timestamp.from(Instant.now()), java.sql.Timestamp.from(Instant.now()));
        
        entityManager.clear();

        // 3. Switch back to Tenant A and verify ONLY memoryA is visible via JPA
        TenantContext.set(tenantA, userA);
        List<Memory> allMemories = memoryRepository.findAll();
        
        assertThat(allMemories).hasSize(1);
        assertThat(allMemories.get(0).getContent()).isEqualTo("Memory for Tenant A");
        assertThat(allMemories.get(0).getTenantId()).isEqualTo(tenantA);

        // 4. Switch to Tenant B and verify ONLY memoryB is visible via JPA
        TenantContext.set(tenantB, userB);
        allMemories = memoryRepository.findAll();
        
        assertThat(allMemories).hasSize(1);
        assertThat(allMemories.get(0).getContent()).isEqualTo("Memory for Tenant B");
        assertThat(allMemories.get(0).getTenantId()).isEqualTo(tenantB);
    }

    private Memory createMemory(UUID tenantId, UUID userId, String content) {
        Memory memory = new Memory();
        memory.setTenantId(tenantId);
        memory.setUserId(userId);
        memory.setContent(content);
        memory.setMemoryType(MemoryType.EPISODIC);
        memory.setSourceConversationId(UUID.randomUUID());
        memory.setSourceSessionId(UUID.randomUUID());
        memory.setEmbeddingModelVersion("text-embedding-3-small");
        memory.setEmbeddingDimension(1536);
        memory.setImportanceScore(0.8);
        memory.setRetrievalCount(0L);
        memory.setLastRetrievedAt(Instant.now());
        memory.setVersion(1);
        memory.setSoftDeleted(false);
        return memory;
    }
}
