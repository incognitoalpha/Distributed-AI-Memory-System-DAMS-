package com.aimemory.embedding.kafka;

import com.aimemory.embedding.service.EmbeddingGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Consumes embedding.requested events and delegates to the generation service.
 * Implements 3-retry exponential backoff with dead-letter routing on exhaustion.
 *
 * @author agent
 * @since 1.0.0
 */
@Component
public class EmbeddingRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingRequestConsumer.class);

    private final EmbeddingGenerationService generationService;
    private final EmbeddingEventPublisher eventPublisher;

    public EmbeddingRequestConsumer(
            EmbeddingGenerationService generationService,
            EmbeddingEventPublisher eventPublisher) {
        this.generationService = generationService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Processes embedding requests from Kafka.
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR,
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "embedding.requested", groupId = "embedding-service")
    public void consume(EmbeddingRequestedEvent event) {
        log.info("Processing embedding request for memoryId={}", event.memoryId());

        try {
            EmbeddingGenerationService.EmbeddingResult result = generationService.generateEmbedding(event.content());

            // Publish embedding.completed event
            eventPublisher.publishEmbeddingCompleted(
                    event.memoryId(),
                    event.tenantId(),
                    event.userId(),
                    result.vector(),
                    result.modelName(),
                    result.modelVersion(),
                    result.dimension(),
                    result.success()
            );

            log.info("Embedding generated for memoryId={}, success={}", event.memoryId(), result.success());

        } catch (EmbeddingGenerationService.EmbeddingGenerationException e) {
            log.error("Embedding generation failed for memoryId={}", event.memoryId(), e);
            throw e; // Re-throw so @RetryableTopic handles retry/DLT routing
        }
    }

    /**
     * Dead-letter handler for permanently failed embedding requests.
     */
    @DltHandler
    public void handleDlt(EmbeddingRequestedEvent event) {
        log.error("DEAD LETTER: Embedding permanently failed for memoryId={}", event.memoryId());

        // Alert via Prometheus metric - publish failure event
        eventPublisher.publishEmbeddingFailed(event.memoryId(), event.tenantId());
    }

    /**
     * Event record for embedding requests.
     */
    public record EmbeddingRequestedEvent(
            String memoryId,
            String tenantId,
            String userId,
            String content,
            String sourceConversationId,
            String sourceSessionId
    ) {
        public UUID memoryIdAsUuid() {
            return UUID.fromString(memoryId);
        }

        public UUID tenantIdAsUuid() {
            return UUID.fromString(tenantId);
        }

        public UUID userIdAsUuid() {
            return UUID.fromString(userId);
        }
    }
}