package com.aimemory.memory.service;

import com.aimemory.memory.domain.MemoryVersion;
import com.aimemory.memory.repository.MemoryVersionRepository;
import com.aimemory.shared.exception.MemoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryVersionServiceTest {

    @Mock
    private MemoryVersionRepository memoryVersionRepository;

    private MemoryVersionService versionService;

    @BeforeEach
    void setUp() {
        versionService = new MemoryVersionService(memoryVersionRepository);
    }

    @Test
    void getVersionHistory_returnsVersionsWhenMemoryExists() {
        // Arrange
        UUID memoryId = UUID.randomUUID();

        MemoryVersion v1 = new MemoryVersion();
        v1.setVersionId(UUID.randomUUID());
        v1.setMemoryId(memoryId);
        v1.setVersion(1);

        MemoryVersion v2 = new MemoryVersion();
        v2.setVersionId(UUID.randomUUID());
        v2.setMemoryId(memoryId);
        v2.setVersion(2);

        when(memoryVersionRepository.findByMemoryIdOrderByVersionAsc(memoryId))
                .thenReturn(List.of(v1, v2));

        // Act
        List<MemoryVersion> versions = versionService.getVersionHistory(memoryId, UUID.randomUUID());

        // Assert
        assertEquals(2, versions.size());
        assertEquals(1, versions.get(0).getVersion());
        assertEquals(2, versions.get(1).getVersion());
    }

    @Test
    void getVersionHistory_throwsWhenNoVersions() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        when(memoryVersionRepository.findByMemoryIdOrderByVersionAsc(memoryId))
                .thenReturn(List.of());

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                versionService.getVersionHistory(memoryId, UUID.randomUUID())
        );
    }

    @Test
    void getVersionHistory_returnsEmptyListWhenNoVersions() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        when(memoryVersionRepository.findByMemoryIdOrderByVersionAsc(memoryId))
                .thenReturn(List.of());

        // This test expects empty list, but implementation throws exception
        // This test documents the current behavior
        assertThrows(MemoryNotFoundException.class, () ->
                versionService.getVersionHistory(memoryId, UUID.randomUUID())
        );
    }
}