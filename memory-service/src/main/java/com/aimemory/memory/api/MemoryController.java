package com.aimemory.memory.api;

import com.aimemory.memory.api.dto.MemoryConflictResponse;
import com.aimemory.memory.api.dto.MemoryCreateRequest;
import com.aimemory.memory.api.dto.MemoryResponse;
import com.aimemory.memory.domain.MemoryVersion;
import com.aimemory.memory.service.ConflictResolutionService;
import com.aimemory.memory.service.MemoryVersionService;
import com.aimemory.memory.service.MemoryWriteService;
import com.aimemory.shared.domain.TenantContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for memory CRUD operations.
 * All tenantId values are extracted from SecurityContext — never from the request body.
 *
 * @author agent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/memories")
public class MemoryController {

    private static final Logger log = LoggerFactory.getLogger(MemoryController.class);

    private final MemoryWriteService writeService;
    private final MemoryVersionService versionService;
    private final ConflictResolutionService conflictService;

    public MemoryController(
            MemoryWriteService writeService,
            MemoryVersionService versionService,
            ConflictResolutionService conflictService) {
        this.writeService = writeService;
        this.versionService = versionService;
        this.conflictService = conflictService;
    }

    /**
     * Creates a new memory record for the authenticated user.
     */
    @PostMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('USER')")
    public ResponseEntity<MemoryResponse> createMemory(
            @Valid @RequestBody MemoryCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        UUID tenantId = TenantContext.getTenantId();
        UUID userId = TenantContext.getUserId();

        log.info("Creating memory for tenantId={} userId={}", tenantId, userId);

        MemoryResponse response = writeService.create(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a memory by ID.
     */
    @GetMapping("/{memoryId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('USER')")
    public ResponseEntity<MemoryResponse> getMemory(
            @PathVariable UUID memoryId,
            @AuthenticationPrincipal UserDetails principal) {

        UUID tenantId = TenantContext.getTenantId();

        MemoryResponse response = writeService.getById(memoryId, tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing memory, creating a new version.
     */
    @PutMapping("/{memoryId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('USER')")
    public ResponseEntity<MemoryResponse> updateMemory(
            @PathVariable UUID memoryId,
            @RequestBody UpdateMemoryRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        UUID tenantId = TenantContext.getTenantId();
        UUID userId = TenantContext.getUserId();

        log.info("Updating memory id={} for tenantId={}", memoryId, tenantId);

        MemoryResponse response = writeService.update(memoryId, request.content(), tenantId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft-deletes a memory.
     */
    @DeleteMapping("/{memoryId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('USER')")
    public ResponseEntity<Void> deleteMemory(
            @PathVariable UUID memoryId,
            @AuthenticationPrincipal UserDetails principal) {

        UUID tenantId = TenantContext.getTenantId();

        log.info("Soft-deleting memory id={} for tenantId={}", memoryId, tenantId);

        writeService.softDelete(memoryId, tenantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the version history for a memory.
     */
    @GetMapping("/{memoryId}/versions")
    @PreAuthorize("hasRole('AGENT') or hasRole('USER')")
    public ResponseEntity<List<MemoryVersion>> getVersionHistory(
            @PathVariable UUID memoryId,
            @AuthenticationPrincipal UserDetails principal) {

        UUID tenantId = TenantContext.getTenantId();

        List<MemoryVersion> versions = versionService.getVersionHistory(memoryId, tenantId);
        return ResponseEntity.ok(versions);
    }

    /**
     * Request record for updating memory content.
     */
    public record UpdateMemoryRequest(String content) {
    }
}