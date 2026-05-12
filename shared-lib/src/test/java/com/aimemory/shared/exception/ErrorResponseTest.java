package com.aimemory.shared.exception;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void of_with_all_params() {
        ErrorResponse response = ErrorResponse.of(404, "NOT_FOUND", "Memory not found", "trace-123");

        assertEquals(404, response.status());
        assertEquals("NOT_FOUND", response.errorCode());
        assertEquals("Memory not found", response.message());
        assertEquals("trace-123", response.traceId());
        assertNotNull(response.timestamp());
    }

    @Test
    void of_without_trace_id_generates_random_uuid() {
        ErrorResponse response = ErrorResponse.of(500, "INTERNAL_ERROR", "Something went wrong");

        assertEquals(500, response.status());
        assertEquals("INTERNAL_ERROR", response.errorCode());
        assertEquals("Something went wrong", response.message());
        assertNotNull(response.traceId());
        assertTrue(response.traceId().matches("[0-9a-f-]{36}")); // UUID format
    }

    @Test
    void timestamp_is_set_on_creation() {
        Instant before = Instant.now();
        ErrorResponse response = ErrorResponse.of(400, "BAD_REQUEST", "Invalid input");
        Instant after = Instant.now();

        assertTrue(response.timestamp().isAfter(before) || response.timestamp().equals(before));
        assertTrue(response.timestamp().isBefore(after) || response.timestamp().equals(after));
    }

    @Test
    void record_equality() {
        ErrorResponse response1 = ErrorResponse.of(404, "NOT_FOUND", "Not found", "trace-1");
        ErrorResponse response2 = ErrorResponse.of(404, "NOT_FOUND", "Not found", "trace-1");
        ErrorResponse response3 = ErrorResponse.of(404, "NOT_FOUND", "Not found", "trace-2");

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
    }

    @Test
    void can_access_all_fields() {
        String traceId = "trace-abc-123";
        ErrorResponse response = ErrorResponse.of(403, "FORBIDDEN", "Access denied", traceId);

        assertAll("ErrorResponse fields", () -> {
            assertEquals(403, response.status());
            assertEquals("FORBIDDEN", response.errorCode());
            assertEquals("Access denied", response.message());
            assertEquals(traceId, response.traceId());
            assertNotNull(response.timestamp());
        });
    }
}