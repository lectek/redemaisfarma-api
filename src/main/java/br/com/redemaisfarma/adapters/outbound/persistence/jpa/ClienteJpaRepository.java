package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {
    // Ex: Optional<ClienteEntity> findByEmail(String email);
}
