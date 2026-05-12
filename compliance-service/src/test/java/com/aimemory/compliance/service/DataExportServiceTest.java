package com.aimemory.compliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

    private DataExportService dataExportService;

    @BeforeEach
    void setUp() {
        dataExportService = new DataExportService();
    }

    @Test
    void exportUserData_returnsMapWithUserInfo() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        Map<String, Object> export = dataExportService.exportUserData(tenantId, userId);

        // Assert
        assertNotNull(export);
        assertEquals(userId.toString(), export.get("userId"));
        assertEquals(tenantId.toString(), export.get("tenantId"));
    }

    @Test
    void exportUserData_includesExportedTimestamp() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        Map<String, Object> export = dataExportService.exportUserData(tenantId, userId);

        // Assert
        assertNotNull(export.get("exportedAt"));
    }

    @Test
    void exportUserData_includesEmptyListsForData() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        Map<String, Object> export = dataExportService.exportUserData(tenantId, userId);

        // Assert
        assertNotNull(export.get("memories"));
        assertNotNull(export.get("versionHistory"));
        assertNotNull(export.get("conflicts"));
    }

    @Test
    void exportUserData_differentUserIdsProduceDifferentExports() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        // Act
        Map<String, Object> export1 = dataExportService.exportUserData(tenantId, userId1);
        Map<String, Object> export2 = dataExportService.exportUserData(tenantId, userId2);

        // Assert
        assertNotEquals(export1.get("userId"), export2.get("userId"));
    }
}