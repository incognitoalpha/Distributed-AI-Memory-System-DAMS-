package com.aimemory.retrieval.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QueryRewriterServiceTest {

    private QueryRewriterService queryRewriterService;

    @BeforeEach
    void setUp() {
        queryRewriterService = new QueryRewriterService();
    }

    @Test
    void generateHypotheticalDocument_returnsNonEmptyString() {
        // Act
        String hydeDocument = queryRewriterService.generateHypotheticalDocument("What is machine learning?");

        // Assert
        assertNotNull(hydeDocument);
        assertFalse(hydeDocument.isEmpty());
        assertTrue(hydeDocument.length() > 50);
    }

    @Test
    void generateHypotheticalDocument_containsQueryTerms() {
        // Arrange
        String query = "artificial intelligence";

        // Act
        String hydeDocument = queryRewriterService.generateHypotheticalDocument(query);

        // Assert
        assertTrue(hydeDocument.contains(query));
    }

    @Test
    void generateHypotheticalDocument_handlesShortQuery() {
        // Act
        String hydeDocument = queryRewriterService.generateHypotheticalDocument("AI");

        // Assert
        assertNotNull(hydeDocument);
        assertFalse(hydeDocument.isEmpty());
    }

    @Test
    void generateHypotheticalDocument_handlesLongQuery() {
        // Arrange
        String longQuery = "What are the best practices for implementing a distributed system with microservices architecture and how does it compare to monolithic architecture?";

        // Act
        String hydeDocument = queryRewriterService.generateHypotheticalDocument(longQuery);

        // Assert
        assertNotNull(hydeDocument);
        assertTrue(hydeDocument.contains("distributed system"));
    }

    @Test
    void rewriteQuery_trimsWhitespace() {
        // Act
        String result = queryRewriterService.rewriteQuery("  test query  ");

        // Assert
        assertEquals("test query", result);
    }

    @Test
    void rewriteQuery_returnsUnmodifiedCleanInput() {
        // Act
        String result = queryRewriterService.rewriteQuery("clean query");

        // Assert
        assertEquals("clean query", result);
    }
}