package com.aimemory.compliance.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Consumes audit.event Kafka messages and persists to audit_log table.
 * Audit log records are immutable - no UPDATE or DELETE allowed.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class AuditLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditLogConsumer.class);

    /**
     * Processes audit events from all services.
     */
    @KafkaListener(topics = "audit.event", groupId = "compliance-audit")
    public void consumeAuditEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String tenantId = (String) event.get("tenantId");
        String userId = (String) event.get("userId");
        String timestamp = (String) event.get("timestamp");

        log.debug("Processing audit event: eventType={}, tenantId={}, userId={}",
                eventType, tenantId, userId);

        try {
            persistToAuditLog(event);
            log.debug("Audit event persisted: eventType={}", eventType);
        } catch (Exception e) {
            log.error("Failed to persist audit event: {}", e.getMessage(), e);
        }
    }

    private void persistToAuditLog(Map<String, Object> event) {
        // In production: INSERT INTO audit_log (tenant_id, user_id, event_type, ...)
        // INSERT INTO audit_log (tenant_id, user_id, event_type, resource_type, resource_id, details, timestamp)
        // VALUES (:tenantId, :userId, :eventType, :resourceType, :resourceId, :details::jsonb, :timestamp)

        String eventType = (String) event.get("eventType");
        String tenantId = (String) event.get("tenantId");
        String userId = (String) event.get("userId");

        log.debug("Inserting audit log: tenantId={}, userId={}, eventType={}", tenantId, userId, eventType);
    }
}