package com.aimemory.shared.exception;

/**
 * Exception thrown when there is a violation of tenant data isolation.
 * This should never happen in normal operation - indicates a security issue.
 *
 * @author agent
 * @since 1.0.0
 */
public final class TenantIsolationException extends AiMemoryException {

    public TenantIsolationException(String message) {
        super("TENANT_ISOLATION_VIOLATION", message);
    }

    public TenantIsolationException(String message, Throwable cause) {
        super("TENANT_ISOLATION_VIOLATION", message, cause);
    }
}