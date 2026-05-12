package com.aimemory.shared.exception;

import java.time.Instant;

/**
 * Standardized error envelope returned by all REST endpoints on failure.
 *
 * @param status    HTTP status code
 * @param errorCode machine-readable error code (e.g., "MEMORY_NOT_FOUND")
 * @param message   human-readable description
 * @param traceId   OpenTelemetry trace ID for log correlation
 * @param timestamp UTC timestamp of the error
 * @author agent
 * @since 1.0.0
 */
public record ErrorResponse(
    int status,
    String errorCode,
    String message,
    String traceId,
    Instant timestamp
) {
    public static ErrorResponse of(int status, String errorCode, String message, String traceId) {
        return new ErrorResponse(status, errorCode, message, traceId, Instant.now());
    }

    public static ErrorResponse of(int status, String errorCode, String message) {
        return of(status, errorCode, message, java.util.UUID.randomUUID().toString());
    }
}