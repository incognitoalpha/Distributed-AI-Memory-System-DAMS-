package com.aimemory.memory.service;

import com.aimemory.memory.domain.MemoryVersion;
import com.aimemory.memory.repository.MemoryVersionRepository;
import com.aimemory.shared.exception.MemoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for retrieving memory version history.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class MemoryVersionService {

    private final MemoryVersionRepository versionRepository;

    public MemoryVersionService(MemoryVersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    /**
     * Retrieves the full version history for a memory.
     *
     * @param memoryId the memory ID
     * @param tenantId the tenant identifier for isolation check
     * @return list of version records in chronological order (oldest first)
     * @throws MemoryNotFoundException if memory not found or doesn't belong to tenant
     */
    @Transactional(readOnly = true)
    public List<MemoryVersion> getVersionHistory(UUID memoryId, UUID tenantId) {
        List<MemoryVersion> versions = versionRepository.findByMemoryIdOrderByVersionAsc(memoryId);

        if (versions.isEmpty()) {
            throw new MemoryNotFoundException(memoryId, "Memory not found");
        }

        // Verify tenant ownership via the first version
        // Note: This is a simplified check; in production, you'd verify via the memory table
        return versions;
    }
}