package com.aimemory.shared.exception;

import java.util.UUID;

/**
 * Base exception class for all AI Memory system exceptions.
 * All service-layer exceptions should extend this class.
 *
 * @author agent
 * @since 1.0.0
 */
public abstract class AiMemoryException extends RuntimeException {

    private final String errorCode;
    private final UUID traceId;

    protected AiMemoryException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.traceId = UUID.randomUUID();
    }

    protected AiMemoryException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.traceId = UUID.randomUUID();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public UUID getTraceId() {
        return traceId;
    }
}