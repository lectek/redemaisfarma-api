/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.Id
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 *  lombok.Generated
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Generated;

@Entity
@Table(name="sync_checkpoint", uniqueConstraints={@UniqueConstraint(name="uk_sync_checkpoint_source", columnNames={"source"})})
public class SyncCheckpoint {
    @Id
    @Column(name="id", nullable=false, length=64)
    private String id;
    @Column(name="source", nullable=false, length=100, unique=true)
    private String source;
    @Column(name="last_since")
    private LocalDateTime lastSince;
    @Column(name="updated_at", insertable=false, updatable=false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.lastSince == null) {
            this.lastSince = LocalDateTime.of(1900, 1, 1, 0, 0);
        }
    }

    @Generated
    public String getId() {
        return this.id;
    }

    @Generated
    public String getSource() {
        return this.source;
    }

    @Generated
    public LocalDateTime getLastSince() {
        return this.lastSince;
    }

    @Generated
    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @Generated
    public void setId(String id) {
        this.id = id;
    }

    @Generated
    public void setSource(String source) {
        this.source = source;
    }

    @Generated
    public void setLastSince(LocalDateTime lastSince) {
        this.lastSince = lastSince;
    }

    @Generated
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Generated
    public SyncCheckpoint() {
    }
}

