package com.aimemory.shared.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AiMemoryExceptionTest {

    @Test
    void exception_stores_error_code_and_message() {
        TestException ex = new TestException("ERR_CODE", "Error message");

        assertEquals("ERR_CODE", ex.getErrorCode());
        assertEquals("Error message", ex.getMessage());
    }

    @Test
    void exception_generates_trace_id() {
        TestException ex = new TestException("ERR_CODE", "Error message");

        assertNotNull(ex.getTraceId());
        assertTrue(ex.getTraceId().toString().matches("[0-9a-f-]{36}"));
    }

    @Test
    void exception_with_cause() {
        RuntimeException cause = new RuntimeException("Original cause");
        TestException ex = new TestException("ERR_CODE", "Error message", cause);

        assertEquals(cause, ex.getCause());
        assertEquals("Error message", ex.getMessage());
    }

    @Test
    void different_exceptions_have_different_trace_ids() {
        TestException ex1 = new TestException("ERR_CODE", "Message 1");
        TestException ex2 = new TestException("ERR_CODE", "Message 2");

        assertNotEquals(ex1.getTraceId(), ex2.getTraceId());
    }

    @Test
    void exception_is_runtime() {
        TestException ex = new TestException("ERR_CODE", "Error message");
        assertThrows(TestException.class, () -> { throw ex; });
    }

    @Test
    void get_message_includes_cause_message() {
        RuntimeException cause = new RuntimeException("Original cause");
        TestException ex = new TestException("ERR_CODE", "Error message", cause);

        assertTrue(ex.getMessage().contains("Error message"));
    }

    // Test helper exception class
    private static class TestException extends AiMemoryException {
        TestException(String errorCode, String message) {
            super(errorCode, message);
        }

        TestException(String errorCode, String message, Throwable cause) {
            super(errorCode, message, cause);
        }
    }
}