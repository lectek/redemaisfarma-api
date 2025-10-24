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

public interface ProdutoJpaRepository
extends JpaRepository<ProdutoEntity, Long> {
    public Optional<ProdutoEntity> findByCodigoBarras(String var1);

    public Optional<ProdutoEntity> findByLegacyId(Long var1);

    public List<ProdutoEntity> findByCodigoBarrasIn(Collection<String> var1);

    public List<ProdutoEntity> findByLegacyIdIn(Collection<Long> var1);

    public Page<ProdutoEntity> findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String var1, String var2, Pageable var3);

    @Query(value="SELECT p FROM ProdutoEntity p WHERE p.imagem IS NULL OR TRIM(p.imagem) = ''")
    public Page<ProdutoEntity> findSemMidia(Pageable var1);

    public Page<ProdutoEntity> findByStatus(ProdutoStatus var1, Pageable var2);

    public Page<ProdutoEntity> findByStatusOrderByDataImportacaoAsc(ProdutoStatus var1, Pageable var2);

    @Query(value="  SELECT p FROM ProdutoEntity p\n  WHERE p.disponivel = true\n    AND p.estoque > 0\n    AND p.precoVenda > 0\n    AND p.destaqueCarrossel = true\n    AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)\n    AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)\n  ORDER BY COALESCE(p.ordemCarrossel, 9999) ASC, p.dataCadastro DESC, p.id DESC\n")
    public List<ProdutoEntity> findCarrossel(Pageable var1);

    @Query(value="  SELECT p FROM ProdutoEntity p\n  WHERE p.disponivel = true\n    AND p.estoque > 0\n    AND p.precoVenda > 0\n    AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)\n    AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)\n  ORDER BY p.dataCadastro DESC, p.id DESC\n")
    public List<ProdutoEntity> findVitrineFallback(Pageable var1);

    @Query(value="select p from ProdutoEntity p order by p.dataCadastro desc")
    public List<ProdutoEntity> findRecent(Pageable var1);

    @Query(value="select p from ProdutoEntity p where p.disponivel = true order by p.dataCadastro desc")
    public List<ProdutoEntity> findRecentDisponiveis(Pageable var1);

    @Query(value="select p from ProdutoEntity p where p.id in :ids")
    public List<ProdutoEntity> findAllByIdIn(Collection<Long> var1);

    @Query(value="select p from ProdutoEntity p where p.disponivel = true and p.id in :ids")
    public List<ProdutoEntity> findAllDisponiveisByIdIn(Collection<Long> var1);

    @Query(value="    select distinct trim(p.categoria)\n    from ProdutoEntity p\n    where p.categoria is not null and trim(p.categoria) <> ''\n    order by trim(p.categoria) asc\n")
    public List<String> findDistinctCategorias();

    default public Page<ProdutoEntity> searchPage(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return this.findAll(pageable);
        }
        return this.findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageable);
    }
}

