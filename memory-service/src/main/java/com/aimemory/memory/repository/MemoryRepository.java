package com.aimemory.memory.repository;

import com.aimemory.memory.domain.Memory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Memory entity with tenant-scoped queries.
 *
 * @author agent
 * @since 1.0.0
 */
@Repository
public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    /**
     * Find all non-deleted memories for a tenant and user.
     */
    @Query("SELECT m FROM Memory m WHERE m.tenantId = :tenantId AND m.userId = :userId AND m.softDeleted = false ORDER BY m.importanceScore DESC")
    List<Memory> findByTenantIdAndUserId(
            @Param("tenantId") UUID tenantId,
            @Param("userId") UUID userId
    );

    /**
     * Find memories by tenant with pagination.
     */
    @Query("SELECT m FROM Memory m WHERE m.tenantId = :tenantId AND m.softDeleted = false")
    Page<Memory> findByTenantId(
            @Param("tenantId") UUID tenantId,
            Pageable pageable
    );

    /**
     * Find memories for reindex by model version.
     */
    @Query("SELECT m FROM Memory m WHERE m.embeddingModelVersion != :currentVersion AND m.softDeleted = false")
    List<Memory> findByOutdatedModelVersion(
            @Param("currentVersion") String currentVersion
    );

    /**
     * Find memories eligible for pruning (low importance, old, not recently retrieved).
     */
    @Query("SELECT m FROM Memory m WHERE m.tenantId = :tenantId " +
           "AND m.importanceScore < :threshold " +
           "AND m.softDeleted = false " +
           "AND m.lastRetrievedAt < :cutoffDate")
    List<Memory> findPruningCandidates(
            @Param("tenantId") UUID tenantId,
            @Param("threshold") double threshold,
            @Param("cutoffDate") java.time.Instant cutoffDate
    );

    /**
     * Check if a memory belongs to the given tenant.
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Memory m WHERE m.memoryId = :memoryId AND m.tenantId = :tenantId")
    boolean belongsToTenant(
            @Param("memoryId") UUID memoryId,
            @Param("tenantId") UUID tenantId
    );

    /**
     * Find memories for a specific conversation.
     */
    List<Memory> findBySourceConversationIdAndTenantIdAndSoftDeleted(
            UUID conversationId,
            UUID tenantId,
            boolean softDeleted
    );
}