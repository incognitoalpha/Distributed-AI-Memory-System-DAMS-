package com.aimemory.compliance.api;

import com.aimemory.compliance.service.DataExportService;
import com.aimemory.compliance.service.ErasureOrchestrationService;
import com.aimemory.shared.domain.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for GDPR compliance operations.
 *
 * @author agent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/gdpr")
public class GdprController {

    private static final Logger log = LoggerFactory.getLogger(GdprController.class);

    private final ErasureOrchestrationService erasureService;
    private final DataExportService exportService;

    public GdprController(
            ErasureOrchestrationService erasureService,
            DataExportService exportService) {
        this.erasureService = erasureService;
        this.exportService = exportService;
    }

    /**
     * Initiates GDPR erasure request for a user.
     */
    @PostMapping("/erasure")
    public ResponseEntity<Map<String, Object>> initiateErasure(
            @RequestBody ErasureRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        UUID userId = request.userId();
        UUID requestId = UUID.randomUUID();

        log.info("Erasure request: requestId={}, tenantId={}, userId={}", requestId, tenantId, userId);

        erasureService.initiateErasure(tenantId, userId, requestId);

        return ResponseEntity.accepted().body(Map.of(
                "requestId", requestId.toString(),
                "status", "PROCESSING",
                "message", "Erasure request accepted. Will complete within 72 hours."
        ));
    }

    /**
     * Checks erasure request status.
     */
    @GetMapping("/erasure/{requestId}")
    public ResponseEntity<Map<String, Object>> getErasureStatus(
            @PathVariable String requestId) {

        // In production: query compliance_requests table
        return ResponseEntity.ok(Map.of(
                "requestId", requestId,
                "status", "PROCESSING" // or "COMPLETED", "FAILED"
        ));
    }

    /**
     * Exports all data for a user (GDPR right to export).
     */
    @GetMapping("/export/{userId}")
    public ResponseEntity<String> exportUserData(
            @PathVariable UUID userId,
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        UUID tenantId = TenantContext.getTenantId();

        log.info("Export request: tenantId={}, userId={}", tenantId, userId);

        Map<String, Object> data = exportService.exportUserData(tenantId, userId);

        String json = data.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "export-" + userId + ".json");

        return ResponseEntity.ok()
                .headers(headers)
                .body(json);
    }

    public record ErasureRequest(UUID userId) {}
}