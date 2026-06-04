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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryWriteServiceTest {

    @Mock
    private MemoryRepository memoryRepository;

    @Mock
    private MemoryVersionRepository memoryVersionRepository;

    @Mock
    private MemoryEventPublisher eventPublisher;

    @Mock
    private ConflictResolutionService conflictResolutionService;

    private MeterRegistry meterRegistry;

    private MemoryWriteService writeService;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        writeService = new MemoryWriteService(
                memoryRepository,
                memoryVersionRepository,
                eventPublisher,
                conflictResolutionService,
                meterRegistry
        );
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void create_savesMemoryWithCorrectFields() {
        // Arrange
        MemoryCreateRequest request = new MemoryCreateRequest(
                "Test memory content",
                MemoryType.EPISODIC,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        when(memoryRepository.save(any(Memory.class))).thenAnswer(invocation -> {
            Memory m = invocation.getArgument(0);
            m.setMemoryId(UUID.randomUUID());
            return m;
        });
        when(memoryVersionRepository.save(any(MemoryVersion.class))).thenReturn(new MemoryVersion());

        // Act
        MemoryResponse response = writeService.create(request, tenantId, userId);

        // Assert
        assertNotNull(response);
        assertEquals("Test memory content", response.content());
        assertEquals(MemoryType.EPISODIC, response.memoryType());
        assertEquals(1, response.version());
        assertEquals(tenantId, response.tenantId());
        assertEquals(userId, response.userId());

        verify(memoryRepository).save(any(Memory.class));
        verify(memoryVersionRepository).save(any(MemoryVersion.class));
        verify(eventPublisher).publishMemoryIngested(any(Memory.class));
    }

    @Test
    void create_setsDefaultEmbeddingValues() {
        // Arrange
        MemoryCreateRequest request = new MemoryCreateRequest(
                "Content",
                MemoryType.SEMANTIC,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ArgumentCaptor<Memory> captor = ArgumentCaptor.forClass(Memory.class);
        when(memoryRepository.save(captor.capture())).thenAnswer(invocation -> {
            Memory m = invocation.getArgument(0);
            m.setMemoryId(UUID.randomUUID());
            return m;
        });
        when(memoryVersionRepository.save(any(MemoryVersion.class))).thenReturn(new MemoryVersion());

        // Act
        writeService.create(request, tenantId, userId);

        // Assert
        Memory saved = captor.getValue();
        assertEquals("text-embedding-3-small-v1", saved.getEmbeddingModelVersion());
        assertEquals(1536, saved.getEmbeddingDimension());
        assertEquals(0.0, saved.getImportanceScore());
        assertEquals(0L, saved.getRetrievalCount());
    }

    @Test
    void update_incrementsVersionAndSetsReplacesMemoryId() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory existing = new Memory();
        existing.setMemoryId(memoryId);
        existing.setTenantId(tenantId);
        existing.setUserId(userId);
        existing.setContent("Old content");
        existing.setVersion(1);
        existing.setSoftDeleted(false);

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
        when(memoryRepository.save(any(Memory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memoryVersionRepository.save(any(MemoryVersion.class))).thenReturn(new MemoryVersion());

        // Act
        MemoryResponse response = writeService.update(memoryId, "New content", tenantId, userId);

        // Assert
        assertEquals(2, response.version());
        assertNull(response.replacesMemoryId());
        verify(memoryVersionRepository).save(any(MemoryVersion.class));
        verify(eventPublisher).publishMemoryVersioned(any(Memory.class));
    }

    @Test
    void update_throwsWhenMemoryNotFound() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        when(memoryRepository.findById(memoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.update(memoryId, "New content", tenantId, userId)
        );
    }

    @Test
    void update_throwsWhenTenantMismatch() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory existing = new Memory();
        existing.setMemoryId(memoryId);
        existing.setTenantId(UUID.randomUUID()); // Different tenant
        existing.setSoftDeleted(false);

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.update(memoryId, "New content", tenantId, userId)
        );
    }

    @Test
    void update_throwsWhenSoftDeleted() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory existing = new Memory();
        existing.setMemoryId(memoryId);
        existing.setTenantId(tenantId);
        existing.setSoftDeleted(true);

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.update(memoryId, "New content", tenantId, userId)
        );
    }

    @Test
    void getById_returnsMemoryWhenFound() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory memory = new Memory();
        memory.setMemoryId(memoryId);
        memory.setTenantId(tenantId);
        memory.setContent("Test content");
        memory.setSoftDeleted(false);

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(memory));

        // Act
        MemoryResponse response = writeService.getById(memoryId, tenantId);

        // Assert
        assertNotNull(response);
        assertEquals(memoryId, response.memoryId());
        assertEquals("Test content", response.content());
    }

    @Test
    void getById_throwsWhenNotFound() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        when(memoryRepository.findById(memoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.getById(memoryId, tenantId)
        );
    }

    @Test
    void getById_throwsWhenTenantMismatch() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory memory = new Memory();
        memory.setMemoryId(memoryId);
        memory.setTenantId(UUID.randomUUID());

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(memory));

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.getById(memoryId, tenantId)
        );
    }

    @Test
    void getById_throwsWhenSoftDeleted() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory memory = new Memory();
        memory.setMemoryId(memoryId);
        memory.setTenantId(tenantId);
        memory.setSoftDeleted(true);

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(memory));

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.getById(memoryId, tenantId)
        );
    }

    @Test
    void softDelete_setsSoftDeletedAndDates() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory memory = new Memory();
        memory.setMemoryId(memoryId);
        memory.setTenantId(tenantId);
        memory.setSoftDeleted(false);

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(memory));
        when(memoryRepository.save(any(Memory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        writeService.softDelete(memoryId, tenantId);

        // Assert
        assertTrue(memory.isSoftDeleted());
        assertNotNull(memory.getSoftDeletedAt());
        assertNotNull(memory.getHardDeleteEligibleAt());
        verify(memoryRepository).save(memory);
    }

    @Test
    void softDelete_throwsWhenNotFound() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        when(memoryRepository.findById(memoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.softDelete(memoryId, tenantId)
        );
    }

    @Test
    void softDelete_throwsWhenTenantMismatch() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        Memory memory = new Memory();
        memory.setMemoryId(memoryId);
        memory.setTenantId(UUID.randomUUID());

        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(memory));

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                writeService.softDelete(memoryId, tenantId)
        );
    }
}
