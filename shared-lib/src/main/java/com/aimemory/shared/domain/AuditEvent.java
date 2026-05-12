package com.aimemory.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record representing an audit event in the system.
 * All audit events are immutable and should never be modified after creation.
 *
 * @author agent
 * @since 1.0.0
 */
public record AuditEvent(
    UUID eventId,
    Instant timestamp,
    String eventType,
    UUID tenantId,
    UUID userId,
    String actorId,
    String action,
    String resourceType,
    UUID resourceId,
    String details,
    String traceId
) {
    /**
     * Factory method to create a new audit event.
     *
     * @param eventType   the type of event (e.g., "MEMORY_CREATED", "MEMORY_ACCESSED")
     * @param tenantId    the tenant ID
     * @param userId      the user ID
     * @param actorId     the ID of the actor performing the action
     * @param action      the action performed (e.g., "CREATE", "READ", "UPDATE", "DELETE")
     * @param resourceType the type of resource being acted upon
     * @param resourceId  the ID of the resource
     * @param details     additional details in JSON format
     * @param traceId     the OpenTelemetry trace ID
     * @return a new AuditEvent instance
     */
    public static AuditEvent of(
            String eventType,
            UUID tenantId,
            UUID userId,
            String actorId,
            String action,
            String resourceType,
            UUID resourceId,
            String details,
            String traceId
    ) {
        return new AuditEvent(
            UUID.randomUUID(),
            Instant.now(),
            eventType,
            tenantId,
            userId,
            actorId,
            action,
            resourceType,
            resourceId,
            details,
            traceId
        );
    }
}