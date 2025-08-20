package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, Long> {
    // Ex: métodos customizados podem ser adicionados aqui se necessário
}
