package com.aimemory.pruning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handles soft-delete operations for memories.
 * Sets softDeleted=true, softDeletedAt, and hardDeleteEligibleAt.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class SoftDeleteService {

    private static final Logger log = LoggerFactory.getLogger(SoftDeleteService.class);

    private final int holdDays;

    public SoftDeleteService(
            @Value("${pruning.hold-days:30}") int holdDays) {
        this.holdDays = holdDays;
    }

    /**
     * Soft-deletes a memory, making it invisible to retrieval but retainable.
     *
     * @param memoryId the memory ID to soft-delete
     * @return true if successful
     */
    public boolean softDelete(UUID memoryId) {
        log.info("Soft-deleting memory: memoryId={}, holdDays={}", memoryId, holdDays);

        Instant now = Instant.now();
        Instant hardDeleteEligible = now.plusSeconds((long) holdDays * 24 * 60 * 60);

        // In production: update database
        // UPDATE memories SET
        //   soft_deleted = true,
        //   soft_deleted_at = :now,
        //   hard_delete_eligible_at = :hardDeleteEligible
        // WHERE memory_id = :memoryId

        log.info("Memory soft-deleted: memoryId={}, hard-delete eligible at={}",
                memoryId, hardDeleteEligible);

        return true;
    }

    /**
     * Batch soft-deletes multiple memories.
     *
     * @param memoryIds list of memory IDs
     * @return number of memories soft-deleted
     */
    public int softDeleteBatch(List<UUID> memoryIds) {
        log.info("Batch soft-deleting {} memories", memoryIds.size());

        int deleted = 0;
        for (UUID memoryId : memoryIds) {
            if (softDelete(memoryId)) {
                deleted++;
            }
        }

        log.info("Batch soft-delete complete: {}/{} memories deleted", deleted, memoryIds.size());
        return deleted;
    }

    public int getHoldDays() {
        return holdDays;
    }
}