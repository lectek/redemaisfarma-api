package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.SyncCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncCheckpointRepository extends JpaRepository<SyncCheckpoint, String> {
    Optional<SyncCheckpoint> findBySource(String source);
}
