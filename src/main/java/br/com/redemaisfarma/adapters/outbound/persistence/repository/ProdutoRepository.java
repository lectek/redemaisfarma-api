/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository
extends JpaRepository<ProdutoEntity, Long> {
    public Optional<ProdutoEntity> findByCodigoBarras(String var1);

    public boolean existsByCodigoBarras(String var1);

    public Optional<ProdutoEntity> findByLegacyId(Long var1);

    public boolean existsByLegacyId(Long var1);

    public List<ProdutoEntity> findAllByCategoria(String var1);

    public Page<ProdutoEntity> findByCategoriaIgnoreCase(String var1, Pageable var2);

    @Query(value="SELECT p FROM ProdutoEntity p WHERE p.estoque < :limite")
    public List<ProdutoEntity> findComEstoqueBaixo(@Param(value="limite") Integer var1);

    public List<ProdutoEntity> findByDisponivelTrue();

    @Query(value="SELECT p FROM ProdutoEntity p WHERE p.imagem IS NULL OR TRIM(p.imagem) = ''")
    public Page<ProdutoEntity> findSemMidia(Pageable var1);

    public Page<ProdutoEntity> findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String var1, String var2, Pageable var3);

    public Page<ProdutoEntity> findByNomeContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String var1, String var2, Pageable var3);

    public List<ProdutoEntity> findTop2000ByOrderByIdAsc();

    public List<ProdutoEntity> findTop2000ByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCaseOrderByIdAsc(String var1, String var2);

    public Optional<ProdutoEntity> findFirstByNomeContainingIgnoreCase(String var1);

    @Query(value="select p from ProdutoEntity p order by p.dataCadastro desc")
    public List<ProdutoEntity> findRecent(Pageable var1);

    @Query(value="select p from ProdutoEntity p where p.disponivel = true order by p.dataCadastro desc")
    public List<ProdutoEntity> findRecentDisponiveis(Pageable var1);

    @Query(value="select p from ProdutoEntity p where p.id in :ids")
    public List<ProdutoEntity> findAllByIdIn(Collection<Long> var1);

    @Query(value="select p from ProdutoEntity p where p.disponivel = true and p.id in :ids")
    public List<ProdutoEntity> findAllDisponiveisByIdIn(Collection<Long> var1);

    @Query(value="select distinct trim(p.categoria)\nfrom ProdutoEntity p\nwhere p.categoria is not null and trim(p.categoria) <> ''\norder by trim(p.categoria) asc\n")
    public List<String> findDistinctCategorias();

    @Query(value="  SELECT p FROM ProdutoEntity p\n  WHERE p.disponivel = true\n    AND p.estoque > 0\n    AND p.precoVenda > 0\n    AND p.destaqueCarrossel = true\n    AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)\n    AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)\n  ORDER BY COALESCE(p.ordemCarrossel, 9999) ASC, p.dataCadastro DESC, p.id DESC\n")
    public List<ProdutoEntity> findCarrossel(Pageable var1);

    @Query(value="  SELECT p FROM ProdutoEntity p\n  WHERE p.disponivel = true\n    AND p.estoque > 0\n    AND p.precoVenda > 0\n    AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)\n    AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)\n  ORDER BY p.dataCadastro DESC, p.id DESC\n")
    public List<ProdutoEntity> findVitrineFallback(Pageable var1);

    public Page<ProdutoEntity> findByStatus(ProdutoStatus var1, Pageable var2);

    public Page<ProdutoEntity> findByStatusOrderByDataImportacaoAsc(ProdutoStatus var1, Pageable var2);

    @Query(value="  SELECT p FROM ProdutoEntity p\n  WHERE\n    (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))\n    AND (\n      :q IS NULL\n      OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))\n      OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))\n      OR p.codigoBarras     LIKE CONCAT('%', :q, '%')\n    )\n")
    public Page<ProdutoEntity> searchPageByCategoria(@Param(value="q") String var1, @Param(value="cat") String var2, Pageable var3);

    @Query(value="  SELECT p FROM ProdutoEntity p\n  WHERE p.disponivel = true AND p.estoque > 0 AND p.precoVenda > 0\n    AND (\n      :q IS NULL\n      OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))\n      OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))\n      OR p.codigoBarras     LIKE CONCAT('%', :q, '%')\n    )\n  ORDER BY p.dataCadastro DESC, p.id DESC\n")
    public Page<ProdutoEntity> searchPublicPage(@Param(value="q") String var1, Pageable var2);

    default public Page<ProdutoEntity> searchPage(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return this.findAll(pageable);
        }
        Page<ProdutoEntity> first = this.findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageable);
        return first.hasContent() ? first : this.findByNomeContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, pageable);
    }

    default public List<ProdutoEntity> searchForExport(String q, int limit) {
        PageRequest pageReq = PageRequest.of((int)0, (int)Math.max(limit, 1), (Sort)Sort.by((String[])new String[]{"id"}).ascending());
        if (q == null || q.isBlank()) {
            return this.findAll((Pageable)pageReq).getContent();
        }
        List a = this.findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, (Pageable)pageReq).getContent();
        if (!a.isEmpty()) {
            return a;
        }
        return this.findByNomeContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(q, q, (Pageable)pageReq).getContent();
    }

    default public List<ProdutoEntity> searchForExportLimited(String q, String categoria, int limit) {
        int pageSize = Math.max(1, Math.min(limit, 2000));
        PageRequest pageReq = PageRequest.of((int)0, (int)pageSize, (Sort)Sort.by((String[])new String[]{"id"}).ascending());
        return this.searchPageByCategoria(q, categoria, (Pageable)pageReq).getContent();
    }
}

