package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório oficial dos produtos no banco de dados novo (MySQL). Responsável por operações CRUD e filtros
 * personalizados.
 */
@Repository
public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {

    /**
     * Busca um produto pelo código de barras.
     *
     * @param codigoBarras
     *            Código de barras do produto
     *
     * @return Produto correspondente, se existir
     */
    Optional<ProdutoEntity> findByCodigoBarras(String codigoBarras);

    /**
     * Verifica se já existe um produto com o código de barras fornecido.
     *
     * @param codigoBarras
     *            Código de barras a verificar
     *
     * @return true se existir, false caso contrário
     */
    boolean existsByCodigoBarras(String codigoBarras);

    /**
     * Retorna todos os produtos de uma categoria específica.
     *
     * @param categoria
     *            Nome da categoria
     *
     * @return Lista de produtos da categoria
     */
    List<ProdutoEntity> findAllByCategoria(String categoria);

    /**
     * Busca produtos com estoque abaixo de um valor mínimo.
     *
     * @param limite
     *            Quantidade mínima de estoque
     *
     * @return Lista de produtos com estoque baixo
     */
    @Query("SELECT p FROM ProdutoEntity p WHERE p.estoque < :limite")
    List<ProdutoEntity> findComEstoqueBaixo(@Param("limite") Integer limite);

    /**
     * Lista todos os produtos marcados como disponíveis para venda.
     *
     * @return Lista de produtos disponíveis
     */
    List<ProdutoEntity> findByDisponivelTrue();
}
