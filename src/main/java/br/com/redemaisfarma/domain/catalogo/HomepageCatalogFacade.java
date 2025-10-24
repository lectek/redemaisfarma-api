/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.HomePageVM;
import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.domain.catalogo.ProdutoQueryService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HomepageCatalogFacade {
    private final ProdutoQueryService produtoQuery;
    private final AppSettingService settings;

    public HomepageCatalogFacade(ProdutoQueryService produtoQuery, AppSettingService settings) {
        this.produtoQuery = produtoQuery;
        this.settings = settings;
    }

    public HomePageVM buildHomepage() {
        int limit = HomepageCatalogFacade.parseIntOrDefault(this.settings.getOrDefault("LAYOUT.layout.itens_por_pagina", "24"), 24);
        boolean exibirIndisp = this.settings.getBoolean("LAYOUT.layout.exibir_indisponiveis", false);
        PageRequest page = PageRequest.of((int)0, (int)Math.min(limit, 8));
        List<ProductCardVM> maisVendidos = this.produtoQuery.topSellers((Pageable)page, exibirIndisp);
        List<ProductCardVM> novidades = this.produtoQuery.newArrivals((Pageable)page, exibirIndisp);
        List<ProductCardVM> destaques = this.produtoQuery.featured((Pageable)page, exibirIndisp);
        List<ProductCardVM> paraVoce = this.produtoQuery.recommended((Pageable)page, exibirIndisp);
        if (paraVoce.isEmpty()) {
            paraVoce = maisVendidos;
        }
        return new HomePageVM(paraVoce, maisVendidos, novidades, destaques);
    }

    private static int parseIntOrDefault(String v, int def) {
        if (v == null || v.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        }
        catch (NumberFormatException e) {
            return def;
        }
    }
}

