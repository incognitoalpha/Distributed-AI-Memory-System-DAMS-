package com.aimemory.embedding.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ModelVersionRegistryTest {

    private ModelVersionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ModelVersionRegistry();
        ReflectionTestUtils.setField(registry, "currentModelName", "text-embedding-3-small");
        ReflectionTestUtils.setField(registry, "currentModelVersion", "v1");
        ReflectionTestUtils.setField(registry, "currentDimension", 1536);
        registry.init();
    }

    @Test
    void init_registersDefaultModel() {
        // Assert - after @PostConstruct, default model should be registered
        ModelVersionRegistry.ModelInfo model = registry.getActiveModel();

        assertNotNull(model);
        assertEquals("text-embedding-3-small", model.modelName());
        assertEquals("v1", model.modelVersion());
        assertEquals(1536, model.dimension());
    }

    @Test
    void registerModel_addsToRegistry() {
        // Act
        registry.registerModel("text-embedding-3-large", "v1", 3072);

        // Assert
        Optional<ModelVersionRegistry.ModelInfo> model = registry.getModelInfo("text-embedding-3-large", "v1");

        assertTrue(model.isPresent());
        assertEquals(3072, model.get().dimension());
    }

    @Test
    void getActiveModel_returnsCurrentModel() {
        // Act
        ModelVersionRegistry.ModelInfo model = registry.getActiveModel();

        // Assert
        assertEquals("text-embedding-3-small", model.modelName());
        assertEquals("v1", model.modelVersion());
    }

    @Test
    void getDimension_returnsActiveModelDimension() {
        // Act
        int dimension = registry.getDimension("text-embedding-3-small");

        // Assert
        assertEquals(1536, dimension);
    }

    @Test
    void isActiveVersion_returnsTrueForActiveVersion() {
        // Assert
        assertTrue(registry.isActiveVersion("text-embedding-3-small", "v1"));
        assertFalse(registry.isActiveVersion("text-embedding-3-small", "v2"));
        assertFalse(registry.isActiveVersion("text-embedding-3-large", "v1"));
    }

    @Test
    void switchActiveModel_updatesActiveModel() {
        // Act
        registry.switchActiveModel("text-embedding-3-large", "v2", 3072);

        // Assert
        assertTrue(registry.isActiveVersion("text-embedding-3-large", "v2"));
        assertEquals(3072, registry.getActiveModel().dimension());
    }

    @Test
    void modelInfo_getFullVersion() {
        // Arrange
        ModelVersionRegistry.ModelInfo model = new ModelVersionRegistry.ModelInfo(
                "text-embedding-3-small", "v1", 1536
        );

        // Assert
        assertEquals("text-embedding-3-small-v1", model.getFullVersion());
    }
}