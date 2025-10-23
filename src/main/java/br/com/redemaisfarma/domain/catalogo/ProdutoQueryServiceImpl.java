// src/main/java/br/com/redemaisfarma/domain/catalogo/ProdutoQueryServiceImpl.java
package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository; // <- usa JPA repo enviado
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.ProductCardVM;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProdutoQueryServiceImpl implements ProdutoQueryService {

    private final ProdutoJpaRepository repo;   // <- ajuste aqui
    private final AppSettingService settings;

    public ProdutoQueryServiceImpl(ProdutoJpaRepository repo, AppSettingService settings) {
        this.repo = repo;
        this.settings = settings;
    }

    /** “Mais vendidos” (fallback: maior estoque primeiro). */
    @Override
    public List<ProductCardVM> topSellers(Pageable page, boolean incluirIndisponiveis) {
        // Se quiser manter o “estoque DESC”:
        var sort = Sort.by(Sort.Direction.DESC, "estoque");
        var p = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);

        var slice = repo.findAll(p);
        List<ProdutoEntity> list = slice.getContent();

        if (!incluirIndisponiveis) {
            list = list.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getDisponivel()))
                    .toList();
        }
        return ProductCardVM.fromList(list);
    }

    /** Novidades: dataCadastro DESC (usa queries do JPA repo). */
    @Override
    public List<ProductCardVM> newArrivals(Pageable page, boolean incluirIndisponiveis) {
        List<ProdutoEntity> list = incluirIndisponiveis
                ? repo.findRecent(page)
                : repo.findRecentDisponiveis(page);
        return ProductCardVM.fromList(list);
    }

    /** Destaques: ids da setting `home.featured.ids` (CSV) preservando a ordem. */
    @Override
    public List<ProductCardVM> featured(Pageable page, boolean incluirIndisponiveis) {
        String csvIds = settings.getOrDefault("home.featured.ids", "");
        List<Long> ids = parseIds(csvIds);

        if (!ids.isEmpty()) {
            List<ProdutoEntity> produtos = incluirIndisponiveis
                    ? repo.findAllByIdIn(ids)
                    : repo.findAllDisponiveisByIdIn(ids);

            Map<Long, ProdutoEntity> byId = produtos.stream()
                    .collect(Collectors.toMap(ProdutoEntity::getId, x -> x));
            List<ProductCardVM> out = new ArrayList<>();
            for (Long id : ids) {
                var p = byId.get(id);
                if (p != null) out.add(ProductCardVM.of(p));
                if (out.size() >= page.getPageSize()) break;
            }
            if (!out.isEmpty()) return out;
        }

        // Fallback: carrossel “oficial” se existir, senão vitrine fallback, senão “mais vendidos”
        var carrossel = repo.findCarrossel(page);
        if (!carrossel.isEmpty()) return ProductCardVM.fromList(carrossel);

        var vitrine = repo.findVitrineFallback(page);
        if (!vitrine.isEmpty()) return ProductCardVM.fromList(vitrine);

        return topSellers(page, incluirIndisponiveis);
    }

    /** Recomendados: amostra aleatória das novidades disponíveis. */
    @Override
    public List<ProductCardVM> recommended(Pageable page, boolean incluirIndisponiveis) {
        int size = Math.max(1, page.getPageSize());
        List<ProdutoEntity> base = incluirIndisponiveis
                ? repo.findRecent(PageRequest.of(0, size * 3))
                : repo.findRecentDisponiveis(PageRequest.of(0, size * 3));
        Collections.shuffle(base);
        return ProductCardVM.fromList(base.stream().limit(size).toList());
    }

    // ---------- helpers ----------
    private List<Long> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
