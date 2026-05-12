package com.aimemory.pruning.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SoftDeleteServiceTest {

    private SoftDeleteService softDeleteService;

    @BeforeEach
    void setUp() {
        softDeleteService = new SoftDeleteService(30);
    }

    @Test
    void softDelete_returnsTrue() {
        // Act
        UUID memoryId = UUID.randomUUID();
        boolean result = softDeleteService.softDelete(memoryId);

        // Assert
        assertTrue(result);
    }

    @Test
    void softDeleteBatch_deletesAll() {
        // Arrange
        List<UUID> memoryIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // Act
        int deleted = softDeleteService.softDeleteBatch(memoryIds);

        // Assert
        assertEquals(3, deleted);
    }

    @Test
    void softDeleteBatch_emptyList() {
        // Act
        int deleted = softDeleteService.softDeleteBatch(List.of());

        // Assert
        assertEquals(0, deleted);
    }

    @Test
    void getHoldDays_returnsConfiguredValue() {
        // Assert
        assertEquals(30, softDeleteService.getHoldDays());
    }
}