package com.aimemory.memory.service;

import com.aimemory.memory.domain.Memory;
import com.aimemory.memory.domain.MemoryConflict;
import com.aimemory.memory.domain.enums.ConflictResolutionStrategy;
import com.aimemory.memory.repository.MemoryConflictRepository;
import com.aimemory.memory.repository.MemoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for detecting and resolving memory conflicts.
 * Detects contradictions via content similarity.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ConflictResolutionService {

    private static final Logger log = LoggerFactory.getLogger(ConflictResolutionService.class);

    @Value("${memory.conflict.similarity-threshold:0.85}")
    private double similarityThreshold;

    @Value("${memory.conflict.resolution-strategy:LATEST_WINS}")
    private ConflictResolutionStrategy defaultStrategy;

    private final MemoryRepository memoryRepository;
    private final MemoryConflictRepository conflictRepository;

    public ConflictResolutionService(
            MemoryRepository memoryRepository,
            MemoryConflictRepository conflictRepository) {
        this.memoryRepository = memoryRepository;
        this.conflictRepository = conflictRepository;
    }

    /**
     * Detects potential conflicts with existing memories and creates conflict records.
     *
     * @param memory the new memory to check for conflicts
     */
    @Transactional
    public void detectAndCreateConflict(Memory memory) {
        log.debug("Checking for conflicts with memoryId={}", memory.getMemoryId());

        List<Memory> recentMemories = memoryRepository.findByTenantIdAndUserId(
                memory.getTenantId(),
                memory.getUserId()
        );

        for (Memory existing : recentMemories) {
            if (existing.getMemoryId().equals(memory.getMemoryId())) {
                continue;
            }

            if (isPotentiallyContradictory(existing.getContent(), memory.getContent())) {
                createConflictRecord(memory, existing);
            }
        }
    }

    /**
     * Simple keyword-based contradiction detection for MVP.
     */
    private boolean isPotentiallyContradictory(String content1, String content2) {
        String lower1 = content1.toLowerCase();
        String lower2 = content2.toLowerCase();

        String[] negationWords = {"not", "never", "no", "don't", "didn't", "won't", "wasn't", "isn't"};

        for (String neg : negationWords) {
            if (lower1.contains(neg) && lower2.contains(neg)) {
                String[] words1 = lower1.split("\\W+");
                String[] words2 = lower2.split("\\W+");

                int sharedContentWords = 0;
                for (String w1 : words1) {
                    if (w1.length() > 4 && containsWord(words2, w1)) {
                        sharedContentWords++;
                    }
                }

                return sharedContentWords >= 3;
            }
        }

        return false;
    }

    private boolean containsWord(String[] words, String target) {
        for (String word : words) {
            if (word.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private void createConflictRecord(Memory memory, Memory existing) {
        boolean exists = conflictRepository.existsByOldMemoryIdAndNewMemoryId(
                existing.getMemoryId(), memory.getMemoryId());

        if (exists) {
            return;
        }

        MemoryConflict conflict = new MemoryConflict();
        conflict.setTenantId(memory.getTenantId());
        conflict.setOldMemoryId(existing.getMemoryId());
        conflict.setNewMemoryId(memory.getMemoryId());
        conflict.setSimilarityScore(1.0); // Placeholder for embedding similarity
        conflict.setResolutionStrategy(defaultStrategy);
        conflict.setResolved(false);

        conflictRepository.save(conflict);

        log.info("Conflict detected between memoryId={} and memoryId={}",
                existing.getMemoryId(), memory.getMemoryId());
    }

    /**
     * Resolves a conflict using the configured strategy.
     *
     * @param conflictId the conflict ID
     * @return the ID of the memory that should be kept
     */
    @Transactional
    public UUID resolveConflict(UUID conflictId) {
        MemoryConflict conflict = conflictRepository.findById(conflictId)
                .orElseThrow(() -> new IllegalArgumentException("Conflict not found: " + conflictId));

        if (conflict.isResolved()) {
            throw new IllegalArgumentException("Conflict already resolved");
        }

        UUID keepMemoryId;

        if (conflict.getResolutionStrategy() == ConflictResolutionStrategy.LATEST_WINS) {
            // Keep the newer memory (newMemoryId)
            keepMemoryId = conflict.getNewMemoryId();

            // Soft-delete the old memory
            memoryRepository.findById(conflict.getOldMemoryId()).ifPresent(m -> {
                m.setSoftDeleted(true);
                m.setSoftDeletedAt(java.time.Instant.now());
                memoryRepository.save(m);
            });
        } else {
            keepMemoryId = conflict.getOldMemoryId();
        }

        conflict.setResolved(true);
        conflictRepository.save(conflict);

        log.info("Conflict {} resolved, keeping memoryId={}", conflictId, keepMemoryId);
        return keepMemoryId;
    }
}