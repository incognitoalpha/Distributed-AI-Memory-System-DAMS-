package com.aimemory.embedding.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class EmbeddingGenerationServiceTest {

    @Mock
    private ModelVersionRegistry modelRegistry;

    private EmbeddingGenerationService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingGenerationService(modelRegistry);
        ReflectionTestUtils.setField(embeddingService, "openaiApiKey", "test-key");
        ReflectionTestUtils.setField(embeddingService, "modelName", "text-embedding-3-small");

        // Mock active model - use lenient for tests that don't need it
        ModelVersionRegistry.ModelInfo modelInfo = new ModelVersionRegistry.ModelInfo(
                "text-embedding-3-small", "v1", 1536
        );
        lenient().when(modelRegistry.getActiveModel()).thenReturn(modelInfo);
    }

    @Test
    void generateEmbedding_returnsValidResult() {
        // Act
        EmbeddingGenerationService.EmbeddingResult result = embeddingService.generateEmbedding("Test text");

        // Assert
        assertNotNull(result);
        assertNotNull(result.requestId());
        assertEquals(1536, result.vector().size());
        assertEquals("text-embedding-3-small", result.modelName());
        assertEquals("v1", result.modelVersion());
        assertTrue(result.success());
    }

    @Test
    void generateEmbedding_differentTextsProduceDifferentVectors() {
        // Act
        EmbeddingGenerationService.EmbeddingResult result1 = embeddingService.generateEmbedding("Hello world");
        EmbeddingGenerationService.EmbeddingResult result2 = embeddingService.generateEmbedding("Different text");

        // Assert
        assertNotEquals(result1.vector(), result2.vector());
    }

    @Test
    void generateEmbeddingFallback_returnsEmptyVector() {
        // Act
        EmbeddingGenerationService.EmbeddingResult result = embeddingService.generateEmbeddingFallback("Test text", new RuntimeException("OpenAI API failed"));

        // Assert
        assertNotNull(result);
        assertTrue(result.vector().isEmpty());
        assertFalse(result.success());
        assertEquals("text-embedding-3-small", result.modelName());
    }

    @Test
    void generateEmbeddings_batchProcessing() {
        // Arrange
        List<String> texts = List.of("Text 1", "Text 2", "Text 3");

        // Act
        List<EmbeddingGenerationService.EmbeddingResult> results = embeddingService.generateEmbeddings(texts);

        // Assert
        assertEquals(3, results.size());
        results.forEach(result -> {
            assertNotNull(result);
            assertEquals(1536, result.vector().size());
            assertTrue(result.success());
        });
    }

    @Test
    void embeddingResult_getVectorSize() {
        // This test doesn't need mocks - just verify the record method works
        var result = new EmbeddingGenerationService.EmbeddingResult(
                java.util.UUID.randomUUID(),
                List.of(0.1, 0.2, 0.3, 0.4, 0.5),
                "text-embedding-3-small",
                "v1",
                1536,
                true
        );

        // Assert
        assertEquals(5, result.getVectorSize());
    }
}