package com.aimemory.memory.service;

import com.aimemory.memory.repository.MemoryRepository;
import com.aimemory.memory.domain.MemoryVersion;
import com.aimemory.memory.repository.MemoryVersionRepository;
import com.aimemory.shared.exception.MemoryNotFoundException;
import com.aimemory.shared.exception.TenantIsolationException;
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
    private final MemoryRepository memoryRepository;

    public MemoryVersionService(MemoryVersionRepository versionRepository, MemoryRepository memoryRepository) {
        this.versionRepository = versionRepository;
        this.memoryRepository = memoryRepository;
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
        if (!memoryRepository.belongsToTenant(memoryId, tenantId)) {
            throw new TenantIsolationException("Tenant cannot access version history for memoryId: " + memoryId);
        }

        List<MemoryVersion> versions = versionRepository.findByMemoryIdOrderByVersionAsc(memoryId);

        if (versions.isEmpty()) {
            throw new MemoryNotFoundException(memoryId, "Memory not found");
        }
        return versions;
    }
}
