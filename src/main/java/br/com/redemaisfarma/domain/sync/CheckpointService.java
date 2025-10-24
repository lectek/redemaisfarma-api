/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.domain.sync;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.SyncCheckpoint;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.SyncCheckpointRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class CheckpointService {
    private final SyncCheckpointRepository repo;

    public CheckpointService(SyncCheckpointRepository repo) {
        this.repo = repo;
    }

    public LocalDateTime readSince(String source, LocalDateTime defaultSince) {
        return this.repo.findBySource(source).map(SyncCheckpoint::getLastSince).orElse(defaultSince);
    }

    public void writeSince(String source, LocalDateTime since) {
        SyncCheckpoint cp = this.repo.findBySource(source).orElseGet(() -> {
            SyncCheckpoint c = new SyncCheckpoint();
            c.setId(source);
            c.setSource(source);
            return c;
        });
        cp.setLastSince(since);
        this.repo.save(cp);
    }

    public LocalDateTime findSinceOrEpoch(String source) {
        return this.readSince(source, LocalDateTime.of(1900, 1, 1, 0, 0));
    }

    public void touch(String source, LocalDateTime now) {
        this.writeSince(source, now);
    }
}

