package com.aimemory.memory.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks every mutation of a memory record for full version history.
 *
 * @author agent
 * @since 1.0.0
 */
@Entity
@Table(name = "memory_versions")
@EntityListeners(AuditingEntityListener.class)
public class MemoryVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID versionId;

    @Column(nullable = false)
    private UUID memoryId;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String changeReason;

    @Column(nullable = false)
    private UUID modifiedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public MemoryVersion() {
    }

    public UUID getVersionId() {
        return versionId;
    }

    public void setVersionId(UUID versionId) {
        this.versionId = versionId;
    }

    public UUID getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(UUID memoryId) {
        this.memoryId = memoryId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public UUID getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UUID modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}