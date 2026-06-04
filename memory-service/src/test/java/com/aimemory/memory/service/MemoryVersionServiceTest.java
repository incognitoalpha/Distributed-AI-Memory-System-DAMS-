package com.aimemory.memory.service;

import com.aimemory.memory.domain.MemoryVersion;
import com.aimemory.memory.repository.MemoryRepository;
import com.aimemory.memory.repository.MemoryVersionRepository;
import com.aimemory.shared.exception.MemoryNotFoundException;
import com.aimemory.shared.exception.TenantIsolationException;
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

    @Mock
    private MemoryRepository memoryRepository;

    private MemoryVersionService versionService;

    @BeforeEach
    void setUp() {
        versionService = new MemoryVersionService(memoryVersionRepository, memoryRepository);
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
        UUID tenantId = UUID.randomUUID();
        when(memoryRepository.belongsToTenant(memoryId, tenantId)).thenReturn(true);
        List<MemoryVersion> versions = versionService.getVersionHistory(memoryId, tenantId);

        // Assert
        assertEquals(2, versions.size());
        assertEquals(1, versions.get(0).getVersion());
        assertEquals(2, versions.get(1).getVersion());
    }

    @Test
    void getVersionHistory_throwsWhenNoVersions() {
        // Arrange
        UUID memoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(memoryRepository.belongsToTenant(memoryId, tenantId)).thenReturn(true);
        when(memoryVersionRepository.findByMemoryIdOrderByVersionAsc(memoryId)).thenReturn(List.of());

        // Act & Assert
        assertThrows(MemoryNotFoundException.class, () ->
                versionService.getVersionHistory(memoryId, tenantId)
        );
    }

    @Test
    void getVersionHistory_throwsWhenTenantMismatch() {
        UUID memoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(memoryRepository.belongsToTenant(memoryId, tenantId)).thenReturn(false);

        assertThrows(TenantIsolationException.class, () -> versionService.getVersionHistory(memoryId, tenantId));
    }
}
