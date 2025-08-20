package br.com.redemaisfarma.adapters.outbound.legacy.repository;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoLegacyRepository extends JpaRepository<ProdutoLegacyEntity, Integer> {

    // 🔍 Busca por código de barras
    Optional<ProdutoLegacyEntity> findByCodigoBarras(String codigoBarras);

    // 🔍 Busca por nome parcial (ex: "dipirona")
    List<ProdutoLegacyEntity> findByNomeContainingIgnoreCase(String nome);

    // 🔍 Produtos com estoque positivo
    List<ProdutoLegacyEntity> findBySaldoGreaterThan(Float minimo);
}
