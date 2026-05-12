package com.aimemory.shared.exception;

/**
 * Exception thrown when a requested memory is not found.
 *
 * @author agent
 * @since 1.0.0
 */
public final class MemoryNotFoundException extends AiMemoryException {

    private final java.util.UUID memoryId;

    public MemoryNotFoundException(java.util.UUID memoryId) {
        super("MEMORY_NOT_FOUND", "Memory not found with id: " + memoryId);
        this.memoryId = memoryId;
    }

    public MemoryNotFoundException(java.util.UUID memoryId, String message) {
        super("MEMORY_NOT_FOUND", message);
        this.memoryId = memoryId;
    }

    public java.util.UUID getMemoryId() {
        return memoryId;
    }
}