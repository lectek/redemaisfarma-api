/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 */
package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, Long> {

    Optional<ProdutoEntity> findByCodigoBarras(String var1);

    Optional<ProdutoEntity> findByLegacyId(Long var1);

    List<ProdutoEntity> findByCodigoBarrasIn(Collection<String> var1);

    List<ProdutoEntity> findByLegacyIdIn(Collection<Long> var1);

    Page<ProdutoEntity> findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String var1, String var2, Pageable var3);

    Page<ProdutoEntity> findByDisponivelTrue(Pageable var1);

    @Query("SELECT p FROM ProdutoEntity p WHERE p.imagem IS NULL OR TRIM(p.imagem) = ''")
    Page<ProdutoEntity> findSemMidia(Pageable var1);

    Page<ProdutoEntity> findByStatus(ProdutoStatus var1, Pageable var2);

    Page<ProdutoEntity> findByStatusOrderByDataImportacaoAsc(ProdutoStatus var1, Pageable var2);

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
    List<ProdutoEntity> findCarrossel(Pageable var1);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY p.dataCadastro DESC, p.id DESC
           """)
    List<ProdutoEntity> findVitrineFallback(Pageable var1);

    @Query("select p from ProdutoEntity p order by p.dataCadastro desc")
    List<ProdutoEntity> findRecent(Pageable var1);

    @Query("select p from ProdutoEntity p where p.disponivel = true order by p.dataCadastro desc")
    List<ProdutoEntity> findRecentDisponiveis(Pageable var1);

    @Query("select p from ProdutoEntity p where p.id in :ids")
    List<ProdutoEntity> findAllByIdIn(@Param("ids") Collection<Long> var1);

    @Query("select p from ProdutoEntity p where p.disponivel = true and p.id in :ids")
    List<ProdutoEntity> findAllDisponiveisByIdIn(@Param("ids") Collection<Long> var1);

    @Query("""
           select distinct trim(p.categoria)
             from ProdutoEntity p
            where p.categoria is not null and trim(p.categoria) <> ''
            order by trim(p.categoria) asc
           """)
    List<String> findDistinctCategorias();

    default Page<ProdutoEntity> searchPage(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return this.findAll(pageable);
        }
        return this.findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageable);
    }
}
