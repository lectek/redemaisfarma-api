/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository
extends JpaRepository<ClienteEntity, Long> {
    public Optional<ClienteEntity> findByEmail(String var1);

    public Optional<ClienteEntity> findByCpf(String var1);

    public boolean existsByEmail(String var1);

    public boolean existsByCpf(String var1);

    public boolean existsByEmailIgnoreCase(String var1);

    public Optional<ClienteEntity> findByEmailIgnoreCase(String var1);

    public Optional<ClienteEntity> findFirstByNomeContainingIgnoreCase(String var1);

    public long countByAtivoTrue();

    default public long countAtivos() {
        return this.countByAtivoTrue();
    }
}

