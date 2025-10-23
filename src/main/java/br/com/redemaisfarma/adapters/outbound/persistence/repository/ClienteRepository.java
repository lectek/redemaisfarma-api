package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    // Básicos
    Optional<ClienteEntity> findByEmail(String email);
    Optional<ClienteEntity> findByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    // >>> Adições
    boolean existsByEmailIgnoreCase(String email);
    Optional<ClienteEntity> findByEmailIgnoreCase(String email);
    Optional<ClienteEntity> findFirstByNomeContainingIgnoreCase(String nome);

    // Conveniência (assumindo campo boolean 'ativo' em ClienteEntity)
    long countByAtivoTrue();

    default long countAtivos() {
        return countByAtivoTrue();
    }
}
