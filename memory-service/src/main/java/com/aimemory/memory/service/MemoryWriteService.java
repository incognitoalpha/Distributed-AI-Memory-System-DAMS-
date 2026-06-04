package com.aimemory.memory.service;

import com.aimemory.memory.api.dto.MemoryCreateRequest;
import com.aimemory.memory.api.dto.MemoryResponse;
import com.aimemory.memory.domain.Memory;
import com.aimemory.memory.domain.MemoryVersion;
import com.aimemory.memory.domain.enums.MemoryType;
import com.aimemory.memory.kafka.MemoryEventPublisher;
import com.aimemory.memory.repository.MemoryRepository;
import com.aimemory.memory.repository.MemoryVersionRepository;
import com.aimemory.shared.exception.MemoryNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for memory write operations including create, update, and soft-delete.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class MemoryWriteService {

    private static final Logger log = LoggerFactory.getLogger(MemoryWriteService.class);

    private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small-v1";
    private static final int DEFAULT_EMBEDDING_DIMENSION = 1536;

    private final MemoryRepository memoryRepository;
    private final MemoryVersionRepository memoryVersionRepository;
    private final MemoryEventPublisher eventPublisher;
    private final ConflictResolutionService conflictResolutionService;

    private final Counter createCounter;
    private final Counter updateCounter;
    private final Counter deleteCounter;

    public MemoryWriteService(
            MemoryRepository memoryRepository,
            MemoryVersionRepository memoryVersionRepository,
            MemoryEventPublisher eventPublisher,
            ConflictResolutionService conflictResolutionService,
            MeterRegistry meterRegistry) {
        this.memoryRepository = memoryRepository;
        this.memoryVersionRepository = memoryVersionRepository;
        this.eventPublisher = eventPublisher;
        this.conflictResolutionService = conflictResolutionService;

        this.createCounter = Counter.builder("memory.write.created")
                .description("Total number of memories created")
                .register(meterRegistry);
        this.updateCounter = Counter.builder("memory.write.updated")
                .description("Total number of memories updated")
                .register(meterRegistry);
        this.deleteCounter = Counter.builder("memory.write.deleted")
                .description("Total number of memories soft-deleted")
                .register(meterRegistry);
    }

    /**
     * Creates a new memory record for the authenticated user.
     *
     * @param request  the memory creation payload
     * @param tenantId the tenant identifier from JWT
     * @param userId   the user identifier from JWT
     * @return the persisted memory response
     */
    @Transactional
    public MemoryResponse create(MemoryCreateRequest request, UUID tenantId, UUID userId) {
        log.info("Creating memory for tenantId={} userId={}", tenantId, userId);

        Memory memory = new Memory();
        memory.setTenantId(tenantId);
        memory.setUserId(userId);
        memory.setContent(request.content());
        memory.setMemoryType(request.memoryType());
        memory.setSourceConversationId(request.sourceConversationId());
        memory.setSourceSessionId(request.sourceSessionId());
        memory.setVersion(1);
        memory.setEmbeddingModelVersion(DEFAULT_EMBEDDING_MODEL);
        memory.setEmbeddingDimension(DEFAULT_EMBEDDING_DIMENSION);
        memory.setImportanceScore(0.0);
        memory.setRetrievalCount(0);
        memory.setLastRetrievedAt(Instant.now());
        memory.setSoftDeleted(false);

        Memory saved = memoryRepository.save(memory);

        // Create initial version record
        MemoryVersion version = new MemoryVersion();
        version.setMemoryId(saved.getMemoryId());
        version.setVersion(1);
        version.setContent(saved.getContent());
        version.setModifiedBy(userId);
        version.setChangeReason("Initial creation");
        memoryVersionRepository.save(version);

        // Check for conflicts with existing memories
        conflictResolutionService.detectAndCreateConflict(saved);

        // Publish Kafka event
        eventPublisher.publishMemoryIngested(saved);

        log.info("Memory created with id={}", saved.getMemoryId());
        return MemoryResponse.from(saved);
    }

    /**
     * Updates an existing memory, creating a new version.
     *
     * @param memoryId the ID of the memory to update
     * @param content  the new content
     * @param tenantId the tenant identifier from JWT
     * @param userId   the user identifier from JWT
     * @return the updated memory response
     * @throws MemoryNotFoundException if memory not found or doesn't belong to tenant
     */
    @Transactional
    public MemoryResponse update(UUID memoryId, String content, UUID tenantId, UUID userId) {
        log.info("Updating memory id={} for tenantId={}", memoryId, tenantId);

        Memory existing = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        if (!existing.getTenantId().equals(tenantId)) {
            throw new MemoryNotFoundException(memoryId);
        }

        if (existing.isSoftDeleted()) {
            throw new MemoryNotFoundException(memoryId, "Cannot update soft-deleted memory");
        }

        // Create new version record for old content
        MemoryVersion version = new MemoryVersion();
        version.setMemoryId(existing.getMemoryId());
        version.setVersion(existing.getVersion());
        version.setContent(existing.getContent());
        version.setModifiedBy(userId);
        version.setChangeReason("Superseded by update");
        memoryVersionRepository.save(version);

        // Update the memory
        existing.setContent(content);
        existing.setVersion(existing.getVersion() + 1);
        existing.setReplacesMemoryId(null);

        Memory saved = memoryRepository.save(existing);

        // Check for conflicts with the new version
        conflictResolutionService.detectAndCreateConflict(saved);

        // Publish versioned event
        eventPublisher.publishMemoryVersioned(saved);

        log.info("Memory updated, new version={}", saved.getVersion());
        return MemoryResponse.from(saved);
    }

    /**
     * Retrieves a memory by ID, ensuring tenant isolation.
     *
     * @param memoryId the memory ID
     * @param tenantId the tenant identifier from JWT
     * @return the memory response
     * @throws MemoryNotFoundException if memory not found or doesn't belong to tenant
     */
    @Transactional(readOnly = true)
    public MemoryResponse getById(UUID memoryId, UUID tenantId) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        if (!memory.getTenantId().equals(tenantId)) {
            throw new MemoryNotFoundException(memoryId);
        }

        if (memory.isSoftDeleted()) {
            throw new MemoryNotFoundException(memoryId, "Memory has been deleted");
        }

        return MemoryResponse.from(memory);
    }

    /**
     * Soft-deletes a memory (marks as deleted but retains for potential recovery).
     *
     * @param memoryId the ID of the memory to delete
     * @param tenantId the tenant identifier from JWT
     * @throws MemoryNotFoundException if memory not found or doesn't belong to tenant
     */
    @Transactional
    public void softDelete(UUID memoryId, UUID tenantId) {
        log.info("Soft-deleting memory id={} for tenantId={}", memoryId, tenantId);

        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        if (!memory.getTenantId().equals(tenantId)) {
            throw new MemoryNotFoundException(memoryId);
        }

        Instant now = Instant.now();
        memory.setSoftDeleted(true);
        memory.setSoftDeletedAt(now);
        memory.setHardDeleteEligibleAt(now.plusSeconds(30L * 24 * 60 * 60)); // 30 days

        memoryRepository.save(memory);

        log.info("Memory soft-deleted, hard-delete eligible at={}", memory.getHardDeleteEligibleAt());
    }
}
