package com.aimemory.memory.repository;

import com.aimemory.memory.domain.MemoryVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MemoryVersion entity.
 *
 * @author agent
 * @since 1.0.0
 */
@Repository
public interface MemoryVersionRepository extends JpaRepository<MemoryVersion, UUID> {

    /**
     * Find all versions for a memory ordered by version ascending.
     */
    List<MemoryVersion> findByMemoryIdOrderByVersionAsc(UUID memoryId);
}