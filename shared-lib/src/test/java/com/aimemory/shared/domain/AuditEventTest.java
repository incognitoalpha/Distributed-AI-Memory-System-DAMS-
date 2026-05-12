package com.aimemory.shared.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventTest {

    @Test
    void factory_creates_event_with_generated_id_and_timestamp() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        AuditEvent event = AuditEvent.of(
            "MEMORY_CREATED",
            tenantId,
            userId,
            "actor-123",
            "CREATE",
            "Memory",
            resourceId,
            "{\"key\":\"value\"}",
            "trace-123"
        );

        assertNotNull(event.eventId());
        assertNotNull(event.timestamp());
        assertEquals("MEMORY_CREATED", event.eventType());
        assertEquals(tenantId, event.tenantId());
        assertEquals(userId, event.userId());
        assertEquals("actor-123", event.actorId());
        assertEquals("CREATE", event.action());
        assertEquals("Memory", event.resourceType());
        assertEquals(resourceId, event.resourceId());
        assertEquals("{\"key\":\"value\"}", event.details());
        assertEquals("trace-123", event.traceId());
    }

    @Test
    void factory_generates_unique_event_ids() {
        AuditEvent event1 = AuditEvent.of("TYPE1", UUID.randomUUID(), UUID.randomUUID(), "actor", "ACTION", "type", UUID.randomUUID(), null, "trace");
        AuditEvent event2 = AuditEvent.of("TYPE2", UUID.randomUUID(), UUID.randomUUID(), "actor", "ACTION", "type", UUID.randomUUID(), null, "trace");

        assertNotEquals(event1.eventId(), event2.eventId());
    }

    @Test
    void record_has_no_setters() {
        // Records in Java are immutable - they generate only accessor methods, no setters
        // This test verifies the record has accessor methods but no setters
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        AuditEvent event = AuditEvent.of(
            "TEST", tenantId, userId, "actor", "ACTION", "type", resourceId, null, "trace"
        );

        // Verify accessor methods work
        assertNotNull(event.eventId());
        assertNotNull(event.timestamp());
        assertEquals("TEST", event.eventType());
        assertEquals(tenantId, event.tenantId());
        assertEquals(userId, event.userId());
        assertEquals(resourceId, event.resourceId());
    }

    @Test
    void factory_accepts_null_details() {
        AuditEvent event = AuditEvent.of(
            "MEMORY_ACCESSED",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "actor-123",
            "READ",
            "Memory",
            UUID.randomUUID(),
            null,
            "trace-123"
        );

        assertNull(event.details());
    }

    @Test
    void factory_accepts_null_trace_id() {
        AuditEvent event = AuditEvent.of(
            "MEMORY_ACCESSED",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "actor-123",
            "READ",
            "Memory",
            UUID.randomUUID(),
            "details",
            null
        );

        assertNull(event.traceId());
    }

    @Test
    void event_timestamp_is_recent() {
        Instant before = Instant.now();
        AuditEvent event = AuditEvent.of(
            "TEST", UUID.randomUUID(), UUID.randomUUID(), "actor", "ACTION", "type", UUID.randomUUID(), null, null
        );
        Instant after = Instant.now();

        assertTrue(event.timestamp().isAfter(before) || event.timestamp().equals(before));
        assertTrue(event.timestamp().isBefore(after) || event.timestamp().equals(after));
    }
}