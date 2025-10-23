// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/entity/SyncCheckpoint.java
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "sync_checkpoint",
    uniqueConstraints = @UniqueConstraint(name = "uk_sync_checkpoint_source", columnNames = "source")
)
@Getter @Setter
@NoArgsConstructor
public class SyncCheckpoint {

    /** PK lógica (ex.: "produtos", "firebird.produtos"). */
    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /** Identificador da fonte do sync (ex.: "produtos"). */
    @Column(name = "source", nullable = false, length = 100, unique = true)
    private String source;

    /** Watermark do último registro processado (ex.: LASTUPDATE do Firebird). */
    @Column(name = "last_since")
    private LocalDateTime lastSince;

    /**
     * Atualizado automaticamente pelo MySQL:
     *   DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
     * Deixamos como somente leitura no JPA.
     */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /* Se quiser também mapear a coluna opcional last_update (existe na tabela):
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    */

    @PrePersist
    public void prePersist() {
        // Valor padrão retroativo caso não informado (1900-01-01)
        if (lastSince == null) {
            lastSince = LocalDateTime.of(1900, 1, 1, 0, 0);
        }
    }
}
