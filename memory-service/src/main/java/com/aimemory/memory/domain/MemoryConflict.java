package com.aimemory.memory.domain;

import com.aimemory.memory.domain.enums.ConflictResolutionStrategy;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Records detected conflicts when newer memories contradict older ones.
 *
 * @author agent
 * @since 1.0.0
 */
@Entity
@Table(name = "memory_conflicts")
@EntityListeners(AuditingEntityListener.class)
public class MemoryConflict {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID conflictId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID oldMemoryId;

    @Column(nullable = false)
    private UUID newMemoryId;

    @Column(nullable = false)
    private double similarityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConflictResolutionStrategy resolutionStrategy;

    @Column(columnDefinition = "TEXT")
    private String resolutionDetails;

    @Column(nullable = false)
    private boolean resolved = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public MemoryConflict() {
    }

    public UUID getConflictId() {
        return conflictId;
    }

    public void setConflictId(UUID conflictId) {
        this.conflictId = conflictId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getOldMemoryId() {
        return oldMemoryId;
    }

    public void setOldMemoryId(UUID oldMemoryId) {
        this.oldMemoryId = oldMemoryId;
    }

    public UUID getNewMemoryId() {
        return newMemoryId;
    }

    public void setNewMemoryId(UUID newMemoryId) {
        this.newMemoryId = newMemoryId;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public ConflictResolutionStrategy getResolutionStrategy() {
        return resolutionStrategy;
    }

    public void setResolutionStrategy(ConflictResolutionStrategy resolutionStrategy) {
        this.resolutionStrategy = resolutionStrategy;
    }

    public String getResolutionDetails() {
        return resolutionDetails;
    }

    public void setResolutionDetails(String resolutionDetails) {
        this.resolutionDetails = resolutionDetails;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}