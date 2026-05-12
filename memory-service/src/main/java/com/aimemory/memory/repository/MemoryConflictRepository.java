package com.aimemory.memory.repository;

import com.aimemory.memory.domain.MemoryConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for MemoryConflict entity.
 *
 * @author agent
 * @since 1.0.0
 */
@Repository
public interface MemoryConflictRepository extends JpaRepository<MemoryConflict, UUID> {

    /**
     * Check if a conflict already exists between two memories.
     */
    boolean existsByOldMemoryIdAndNewMemoryId(UUID oldMemoryId, UUID newMemoryId);
}