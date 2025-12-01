package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.ProductCardVM;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProdutoQueryServiceImpl implements ProdutoQueryService {
    private final ProdutoJpaRepository repo;
    private final AppSettingService settings;

    public ProdutoQueryServiceImpl(ProdutoJpaRepository repo, AppSettingService settings) {
        this.repo = repo;
        this.settings = settings;
    }

    @Override
    public List<ProductCardVM> topSellers(Pageable page, boolean incluirIndisponiveis) {
        Sort sort = Sort.by(Sort.Direction.DESC, "estoque");
        PageRequest p = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
        Page<ProdutoEntity> slice = repo.findAll(p);

        List<ProdutoEntity> list = slice.getContent();
        if (!incluirIndisponiveis) {
            list = list.stream().filter(e -> Boolean.TRUE.equals(e.getDisponivel())).toList();
        }
        return ProductCardVM.fromList(list);
    }

    @Override
    public List<ProductCardVM> newArrivals(Pageable page, boolean incluirIndisponiveis) {
        List<ProdutoEntity> list = incluirIndisponiveis ? repo.findRecent(page) : repo.findRecentDisponiveis(page);
        return ProductCardVM.fromList(list);
    }

    @Override
    public List<ProductCardVM> featured(Pageable page, boolean incluirIndisponiveis) {
        String csvIds = settings.getOrDefault("home.featured.ids", "");
        List<Long> ids = parseIds(csvIds);

        if (!ids.isEmpty()) {
            List<ProdutoEntity> produtos = incluirIndisponiveis ? repo.findAllByIdIn(ids) : repo.findAllDisponiveisByIdIn(ids);
            Map<Long, ProdutoEntity> byId = produtos.stream().collect(Collectors.toMap(ProdutoEntity::getId, x -> x));

            List<ProductCardVM> out = new ArrayList<>();
            for (Long id : ids) {
                ProdutoEntity p = byId.get(id);
                if (p != null) out.add(ProductCardVM.of(p));
                if (out.size() >= page.getPageSize()) break;
            }
            if (!out.isEmpty()) return out;
        }

        List<ProdutoEntity> carrossel = repo.findCarrossel(page);
        if (!carrossel.isEmpty()) return ProductCardVM.fromList(carrossel);

        List<ProdutoEntity> vitrine = repo.findVitrineFallback(page);
        if (!vitrine.isEmpty()) return ProductCardVM.fromList(vitrine);

        return topSellers(page, incluirIndisponiveis);
    }

    @Override
    public List<ProductCardVM> recommended(Pageable page, boolean incluirIndisponiveis) {
        int size = Math.max(1, page.getPageSize());
        List<ProdutoEntity> base = incluirIndisponiveis
                ? repo.findRecent(PageRequest.of(0, size * 3))
                : repo.findRecentDisponiveis(PageRequest.of(0, size * 3));
        Collections.shuffle(base);
        return ProductCardVM.fromList(base.stream().limit(size).toList());
    }

    private List<Long> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    try { return Long.parseLong(s); }
                    catch (NumberFormatException e) { return null; }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
