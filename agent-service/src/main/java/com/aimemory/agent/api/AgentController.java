package com.aimemory.agent.api;

import com.aimemory.agent.service.AgentOrchestrationService;
import com.aimemory.shared.domain.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for agent conversation operations.
 *
 * @author agent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final AgentOrchestrationService orchestrationService;

    public AgentController(AgentOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    /**
     * Processes a user message and returns an agent response.
     */
    @PostMapping("/converse")
    public ResponseEntity<Map<String, Object>> converse(
            @RequestBody ConversationRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        UUID userId = TenantContext.getUserId();

        log.info("Conversation request: tenantId={}, userId={}", tenantId, userId);

        UUID conversationId = request.conversationId() != null
                ? request.conversationId()
                : UUID.randomUUID();

        AgentOrchestrationService.AgentResponse response = orchestrationService.processMessage(
                request.message(),
                tenantId,
                userId,
                conversationId
        );

        return ResponseEntity.ok(Map.of(
                "response", response.response(),
                "conversationId", conversationId.toString(),
                "memoriesUsed", response.memoriesUsed(),
                "error", response.error()
        ));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    public record ConversationRequest(String message, UUID conversationId) {}
}