package com.aimemory.agent.service;

import com.aimemory.agent.client.RetrievalServiceGrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates the agent conversation flow.
 * Receives user message → retrieves memories → builds context → calls LLM → returns response.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class AgentOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrationService.class);

    private static final int DEFAULT_TOKEN_BUDGET = 4000;

    private final RetrievalServiceGrpcClient retrievalClient;
    private final ContextBuilderService contextBuilder;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public AgentOrchestrationService(
            RetrievalServiceGrpcClient retrievalClient,
            ContextBuilderService contextBuilder,
            KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.retrievalClient = retrievalClient;
        this.contextBuilder = contextBuilder;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Processes a user conversation message.
     *
     * @param message the user message
     * @param tenantId the tenant ID
     * @param userId the user ID
     * @param conversationId the conversation ID
     * @return agent response
     */
    public AgentResponse processMessage(
            String message,
            UUID tenantId,
            UUID userId,
            UUID conversationId) {

        log.info("Processing message: tenantId={}, userId={}, conversationId={}",
                tenantId, userId, conversationId);

        try {
            // Step 1: Retrieve relevant memories
            var memories = retrievalClient.retrieveMemories(message, tenantId, userId, DEFAULT_TOKEN_BUDGET);
            log.debug("Retrieved {} memories", memories.size());

            // Step 2: Build context prompt
            String prompt = contextBuilder.buildPrompt(message, memories);

            // Step 3: Call LLM (placeholder - in production use LangChain4j)
            String response = callLlm(prompt);

            // Step 4: Publish conversation event to Kafka
            publishConversationEvent(tenantId, userId, conversationId, message, response);

            // Step 5: Extract new memories from response (async via Kafka)
            extractAndIngestMemories(tenantId, userId, conversationId, response);

            return new AgentResponse(response, memories.size(), false);

        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage(), e);
            return new AgentResponse("I encountered an error processing your request.", 0, true);
        }
    }

    private String callLlm(String prompt) {
        // In production: use LangChain4j to call OpenAI or other LLM
        // For development: return simple response
        return "This is a placeholder response from the LLM. " +
                "In production, the actual LLM would process the prompt and return a response.";
    }

    private void publishConversationEvent(UUID tenantId, UUID userId, UUID conversationId,
                                         String userMessage, String agentResponse) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "AGENT_CONVERSATION");
        event.put("tenantId", tenantId.toString());
        event.put("userId", userId.toString());
        event.put("conversationId", conversationId.toString());
        event.put("userMessage", userMessage);
        event.put("agentResponse", agentResponse);
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("agent.conversation", conversationId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish conversation event", ex);
                    }
                });
    }

    private void extractAndIngestMemories(UUID tenantId, UUID userId, UUID conversationId, String response) {
        // In production: use LangChain4j to extract facts from response
        // Then publish to memory.ingested topic for async processing

        log.debug("Memory extraction triggered for conversationId={}", conversationId);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MEMORY_EXTRACTION_REQUESTED");
        event.put("tenantId", tenantId.toString());
        event.put("userId", userId.toString());
        event.put("conversationId", conversationId.toString());
        event.put("agentResponse", response);
        event.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("agent.conversation", conversationId.toString(), event);
    }

    public record AgentResponse(String response, int memoriesUsed, boolean error) {}
}