package com.aimemory.memory.service;

import com.aimemory.memory.domain.Memory;
import com.aimemory.memory.domain.MemoryConflict;
import com.aimemory.memory.domain.enums.ConflictResolutionStrategy;
import com.aimemory.memory.repository.MemoryConflictRepository;
import com.aimemory.memory.repository.MemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConflictResolutionServiceTest {

    @Mock
    private MemoryRepository memoryRepository;

    @Mock
    private MemoryConflictRepository conflictRepository;

    private ConflictResolutionService conflictService;

    @BeforeEach
    void setUp() {
        conflictService = new ConflictResolutionService(memoryRepository, conflictRepository);
        ReflectionTestUtils.setField(conflictService, "similarityThreshold", 0.85);
        ReflectionTestUtils.setField(conflictService, "defaultStrategy", ConflictResolutionStrategy.LATEST_WINS);
    }

    @Test
    void detectAndCreateConflict_noConflictForDifferentUsers() {
        // Arrange
        Memory memory = new Memory();
        memory.setMemoryId(UUID.randomUUID());
        memory.setTenantId(UUID.randomUUID());
        memory.setUserId(UUID.randomUUID());
        memory.setContent("I like pizza");

        when(memoryRepository.findByTenantIdAndUserId(any(), any())).thenReturn(java.util.List.of());

        // Act
        conflictService.detectAndCreateConflict(memory);

        // Assert
        verify(conflictRepository, never()).save(any());
    }

    @Test
    void detectAndCreateConflict_createsConflictForContradictoryContent() {
        // Arrange - content with negation words and >= 3 shared words > 4 chars
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Memory existing = new Memory();
        existing.setMemoryId(UUID.randomUUID());
        existing.setTenantId(tenantId);
        existing.setUserId(userId);
        existing.setContent("I never go to that restaurant because the service was terrible");

        Memory newMemory = new Memory();
        newMemory.setMemoryId(UUID.randomUUID());
        newMemory.setTenantId(tenantId);
        newMemory.setUserId(userId);
        newMemory.setContent("I never go to that restaurant because the prices are too high");

        when(memoryRepository.findByTenantIdAndUserId(tenantId, userId))
                .thenReturn(java.util.List.of(existing));
        when(conflictRepository.existsByOldMemoryIdAndNewMemoryId(any(), any()))
                .thenReturn(false);

        // Act
        conflictService.detectAndCreateConflict(newMemory);

        // Assert
        verify(conflictRepository).save(any(MemoryConflict.class));
    }

    @Test
    void resolveConflict_latestWins_softDeletesOld() {
        // Arrange
        UUID conflictId = UUID.randomUUID();
        UUID oldMemoryId = UUID.randomUUID();
        UUID newMemoryId = UUID.randomUUID();

        MemoryConflict conflict = new MemoryConflict();
        conflict.setConflictId(conflictId);
        conflict.setOldMemoryId(oldMemoryId);
        conflict.setNewMemoryId(newMemoryId);
        conflict.setResolutionStrategy(ConflictResolutionStrategy.LATEST_WINS);
        conflict.setResolved(false);

        Memory oldMemory = new Memory();
        oldMemory.setMemoryId(oldMemoryId);

        when(conflictRepository.findById(conflictId)).thenReturn(Optional.of(conflict));
        when(memoryRepository.findById(oldMemoryId)).thenReturn(Optional.of(oldMemory));
        when(memoryRepository.save(any(Memory.class))).thenReturn(oldMemory);

        // Act
        UUID keptMemoryId = conflictService.resolveConflict(conflictId);

        // Assert
        assertEquals(newMemoryId, keptMemoryId);
        assertTrue(oldMemory.isSoftDeleted());
        assertNotNull(oldMemory.getSoftDeletedAt());
        assertTrue(conflict.isResolved());
    }

    @Test
    void resolveConflict_throwsWhenNotFound() {
        // Arrange
        UUID conflictId = UUID.randomUUID();
        when(conflictRepository.findById(conflictId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                conflictService.resolveConflict(conflictId)
        );
    }

    @Test
    void resolveConflict_throwsWhenAlreadyResolved() {
        // Arrange
        UUID conflictId = UUID.randomUUID();
        MemoryConflict conflict = new MemoryConflict();
        conflict.setConflictId(conflictId);
        conflict.setResolved(true);

        when(conflictRepository.findById(conflictId)).thenReturn(Optional.of(conflict));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                conflictService.resolveConflict(conflictId)
        );
    }
}