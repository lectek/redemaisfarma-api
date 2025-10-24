/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.domain.catalogo.ProdutoQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProdutoQueryServiceImpl
implements ProdutoQueryService {
    private final ProdutoJpaRepository repo;
    private final AppSettingService settings;

    public ProdutoQueryServiceImpl(ProdutoJpaRepository repo, AppSettingService settings) {
        this.repo = repo;
        this.settings = settings;
    }

    @Override
    public List<ProductCardVM> topSellers(Pageable page, boolean incluirIndisponiveis) {
        Sort sort = Sort.by((Sort.Direction)Sort.Direction.DESC, (String[])new String[]{"estoque"});
        PageRequest p = PageRequest.of((int)page.getPageNumber(), (int)page.getPageSize(), (Sort)sort);
        Page slice = this.repo.findAll((Pageable)p);
        List<ProdutoEntity> list = slice.getContent();
        if (!incluirIndisponiveis) {
            list = list.stream().filter(e -> Boolean.TRUE.equals(e.getDisponivel())).toList();
        }
        return ProductCardVM.fromList(list);
    }

    @Override
    public List<ProductCardVM> newArrivals(Pageable page, boolean incluirIndisponiveis) {
        List<ProdutoEntity> list = incluirIndisponiveis ? this.repo.findRecent(page) : this.repo.findRecentDisponiveis(page);
        return ProductCardVM.fromList(list);
    }

    @Override
    public List<ProductCardVM> featured(Pageable page, boolean incluirIndisponiveis) {
        List<ProdutoEntity> carrossel;
        String csvIds = this.settings.getOrDefault("home.featured.ids", "");
        List<Long> ids = this.parseIds(csvIds);
        if (!ids.isEmpty()) {
            List<ProdutoEntity> produtos = incluirIndisponiveis ? this.repo.findAllByIdIn(ids) : this.repo.findAllDisponiveisByIdIn(ids);
            Map<Long, ProdutoEntity> byId = produtos.stream().collect(Collectors.toMap(ProdutoEntity::getId, x -> x));
            ArrayList<ProductCardVM> out = new ArrayList<ProductCardVM>();
            for (Long id : ids) {
                ProdutoEntity p = byId.get(id);
                if (p != null) {
                    out.add(ProductCardVM.of(p));
                }
                if (out.size() < page.getPageSize()) continue;
                break;
            }
            if (!out.isEmpty()) {
                return out;
            }
        }
        if (!(carrossel = this.repo.findCarrossel(page)).isEmpty()) {
            return ProductCardVM.fromList(carrossel);
        }
        List<ProdutoEntity> vitrine = this.repo.findVitrineFallback(page);
        if (!vitrine.isEmpty()) {
            return ProductCardVM.fromList(vitrine);
        }
        return this.topSellers(page, incluirIndisponiveis);
    }

    @Override
    public List<ProductCardVM> recommended(Pageable page, boolean incluirIndisponiveis) {
        int size = Math.max(1, page.getPageSize());
        List<ProdutoEntity> base = incluirIndisponiveis ? this.repo.findRecent((Pageable)PageRequest.of((int)0, (int)(size * 3))) : this.repo.findRecentDisponiveis((Pageable)PageRequest.of((int)0, (int)(size * 3)));
        Collections.shuffle(base);
        return ProductCardVM.fromList(base.stream().limit(size).toList());
    }

    private List<Long> parseIds(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split("[,;\\s]+")).map(String::trim).filter(s -> !s.isBlank()).map(s -> {
            try {
                return Long.parseLong(s);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }
}

