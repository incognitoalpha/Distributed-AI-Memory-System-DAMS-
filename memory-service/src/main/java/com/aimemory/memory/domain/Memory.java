package com.aimemory.memory.domain;

import com.aimemory.memory.domain.enums.MemoryType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Core domain entity representing a single memory record.
 * Implements versioning, provenance tracking, and soft-delete lifecycle.
 *
 * @author agent
 * @since 1.0.0
 */
@Entity
@Table(name = "memories")
@EntityListeners(AuditingEntityListener.class)
public class Memory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID memoryId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryType memoryType;

    @Column(nullable = false)
    private UUID sourceConversationId;

    @Column(nullable = false)
    private UUID sourceSessionId;

    @Column(nullable = false)
    private int version = 1;

    @Column
    private UUID replacesMemoryId;

    @Column(nullable = false)
    private String embeddingModelVersion;

    @Column(nullable = false)
    private int embeddingDimension;

    @Column(nullable = false)
    private double importanceScore;

    @Column(nullable = false)
    private long retrievalCount = 0;

    @Column(nullable = false)
    private Instant lastRetrievedAt;

    @Column(nullable = false)
    private boolean softDeleted = false;

    @Column
    private Instant softDeletedAt;

    @Column
    private Instant hardDeleteEligibleAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public Memory() {
    }

    public UUID getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(UUID memoryId) {
        this.memoryId = memoryId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

    public UUID getSourceConversationId() {
        return sourceConversationId;
    }

    public void setSourceConversationId(UUID sourceConversationId) {
        this.sourceConversationId = sourceConversationId;
    }

    public UUID getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(UUID sourceSessionId) {
        this.sourceSessionId = sourceSessionId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public UUID getReplacesMemoryId() {
        return replacesMemoryId;
    }

    public void setReplacesMemoryId(UUID replacesMemoryId) {
        this.replacesMemoryId = replacesMemoryId;
    }

    public String getEmbeddingModelVersion() {
        return embeddingModelVersion;
    }

    public void setEmbeddingModelVersion(String embeddingModelVersion) {
        this.embeddingModelVersion = embeddingModelVersion;
    }

    public int getEmbeddingDimension() {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(int embeddingDimension) {
        this.embeddingDimension = embeddingDimension;
    }

    public double getImportanceScore() {
        return importanceScore;
    }

    public void setImportanceScore(double importanceScore) {
        this.importanceScore = importanceScore;
    }

    public long getRetrievalCount() {
        return retrievalCount;
    }

    public void setRetrievalCount(long retrievalCount) {
        this.retrievalCount = retrievalCount;
    }

    public Instant getLastRetrievedAt() {
        return lastRetrievedAt;
    }

    public void setLastRetrievedAt(Instant lastRetrievedAt) {
        this.lastRetrievedAt = lastRetrievedAt;
    }

    public boolean isSoftDeleted() {
        return softDeleted;
    }

    public void setSoftDeleted(boolean softDeleted) {
        this.softDeleted = softDeleted;
    }

    public Instant getSoftDeletedAt() {
        return softDeletedAt;
    }

    public void setSoftDeletedAt(Instant softDeletedAt) {
        this.softDeletedAt = softDeletedAt;
    }

    public Instant getHardDeleteEligibleAt() {
        return hardDeleteEligibleAt;
    }

    public void setHardDeleteEligibleAt(Instant hardDeleteEligibleAt) {
        this.hardDeleteEligibleAt = hardDeleteEligibleAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}