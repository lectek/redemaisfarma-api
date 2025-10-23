// src/main/java/br/com/redemaisfarma/domain/sync/CheckpointService.java
package br.com.redemaisfarma.domain.sync;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.SyncCheckpoint;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.SyncCheckpointRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CheckpointService {

    private final SyncCheckpointRepository repo;

    public CheckpointService(SyncCheckpointRepository repo) {
        this.repo = repo;
    }

    public LocalDateTime readSince(String source, LocalDateTime defaultSince) {
        return repo.findBySource(source)
                .map(SyncCheckpoint::getLastSince)
                .orElse(defaultSince);
    }

    public void writeSince(String source, LocalDateTime since) {
        SyncCheckpoint cp = repo.findBySource(source).orElseGet(() -> {
            SyncCheckpoint c = new SyncCheckpoint();
            c.setId(source);          // 👈 PK obrigatória
            c.setSource(source);      // 👈 UK
            return c;
        });
        cp.setLastSince(since);
        repo.save(cp);
    }

    /** Sugiro usar 1900 para garantir “full crawl” inicial. */
    public LocalDateTime findSinceOrEpoch(String source) {
        return readSince(source, LocalDateTime.of(1900,1,1,0,0));
    }

    public void touch(String source, LocalDateTime now) {
        writeSince(source, now);
    }
}