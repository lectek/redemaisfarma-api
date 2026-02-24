package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {

    Optional<ProdutoEntity> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    Optional<ProdutoEntity> findByCodigoOriginal(Long codigoOriginal);

    boolean existsByCodigoOriginal(Long codigoOriginal);

    Optional<ProdutoEntity> findByLegacyId(Long legacyId);

    boolean existsByLegacyId(Long legacyId);

    List<ProdutoEntity> findAllByCategoria(String categoria);

    Page<ProdutoEntity> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    long countByCategoriaIgnoreCase(String categoria);

    @Query("SELECT p FROM ProdutoEntity p WHERE p.estoque <= :limite")
    List<ProdutoEntity> findComEstoqueBaixo(@Param("limite") Integer limite);

    List<ProdutoEntity> findByDisponivelTrue();

    @Query("SELECT p FROM ProdutoEntity p WHERE p.imagem IS NULL OR TRIM(p.imagem) = ''")
    Page<ProdutoEntity> findSemMidia(Pageable pageable);

    Page<ProdutoEntity> findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(
            String descricao, String codigoBarras, Pageable pageable);

    Page<ProdutoEntity> findByNomeContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(
            String nome, String codigoBarras, Pageable pageable);

    List<ProdutoEntity> findTop2000ByOrderByIdAsc();

    List<ProdutoEntity> findTop2000ByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCaseOrderByIdAsc(
            String descricao, String codigoBarras);

    Optional<ProdutoEntity> findFirstByNomeContainingIgnoreCase(String nome);

    @Query("select p from ProdutoEntity p order by p.dataCadastro desc")
    List<ProdutoEntity> findRecent(Pageable pageable);

    @Query("select p from ProdutoEntity p where p.disponivel = true order by p.dataCadastro desc")
    List<ProdutoEntity> findRecentDisponiveis(Pageable pageable);

    @Query("select p from ProdutoEntity p where p.id in :ids")
    List<ProdutoEntity> findAllByIdIn(Collection<Long> ids);

    @Query("select p from ProdutoEntity p where p.disponivel = true and p.id in :ids")
    List<ProdutoEntity> findAllDisponiveisByIdIn(Collection<Long> ids);

    @Query("""
            select distinct trim(p.categoria)
            from ProdutoEntity p
            where p.categoria is not null and trim(p.categoria) <> ''
            order by trim(p.categoria) asc
            """)
    List<String> findDistinctCategorias();

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND p.destaqueCarrossel = true
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY COALESCE(p.ordemCarrossel, 9999) ASC, p.dataCadastro DESC, p.id DESC
            """)
    List<ProdutoEntity> findCarrossel(Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    List<ProdutoEntity> findVitrineFallback(Pageable pageable);

    Page<ProdutoEntity> findByStatus(ProdutoStatus status, Pageable pageable);

    Page<ProdutoEntity> findByStatusOrderByDataImportacaoAsc(ProdutoStatus status, Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE
              (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<ProdutoEntity> searchPageByCategoria(@Param("q") String q,
                                              @Param("cat") String categoria,
                                              Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true AND p.estoque > 0 AND p.precoVenda > 0
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    Page<ProdutoEntity> searchPublicPage(@Param("q") String q, Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true AND p.estoque > 0 AND p.precoVenda > 0
              AND (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    Page<ProdutoEntity> searchPublicPageByCategoria(@Param("q") String q,
                                                    @Param("cat") String categoria,
                                                    Pageable pageable);

    /* ===================== DEFAULT METHODS (tipadas) ===================== */

    default Page<ProdutoEntity> searchPage(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return this.findAll(pageable);
        }
        Page<ProdutoEntity> first =
                this.findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageable);
        return first.hasContent()
                ? first
                : this.findByNomeContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageable);
    }

    default List<ProdutoEntity> searchForExport(String q, int limit) {
        PageRequest pageReq = PageRequest.of(0, Math.max(limit, 1), Sort.by("id").ascending());
        if (q == null || q.isBlank()) {
            return this.findAll(pageReq).getContent();
        }
        List<ProdutoEntity> a =
                this.findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageReq)
                        .getContent();
        if (!a.isEmpty()) {
            return a;
        }
        return this.findByNomeContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageReq)
                .getContent();
    }

    default List<ProdutoEntity> searchForExportLimited(String q, String categoria, int limit) {
        int pageSize = Math.max(1, Math.min(limit, 2000));
        PageRequest pageReq = PageRequest.of(0, pageSize, Sort.by("id").ascending());
        return this.searchPageByCategoria(q, categoria, pageReq).getContent();
    }
}
