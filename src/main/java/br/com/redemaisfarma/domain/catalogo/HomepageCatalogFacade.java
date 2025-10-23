// src/main/java/br/com/redemaisfarma/domain/catalogo/HomepageCatalogFacade.java
package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.HomePageVM;
import br.com.redemaisfarma.application.view.ProductCardVM;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomepageCatalogFacade {

    private final ProdutoQueryService produtoQuery;
    private final AppSettingService settings;

    public HomepageCatalogFacade(ProdutoQueryService produtoQuery, AppSettingService settings) {
        this.produtoQuery = produtoQuery;
        this.settings = settings;
    }

    public HomePageVM buildHomepage() {
        // Lê as chaves em formato "categoria.chave" (string única) e usa defaults
        int limit = parseIntOrDefault(
                settings.getOrDefault("LAYOUT.layout.itens_por_pagina", "24"), 24);

        boolean exibirIndisp = settings.getBoolean(
                "LAYOUT.layout.exibir_indisponiveis", false);

        var page = PageRequest.of(0, Math.min(limit, 8));

        List<ProductCardVM> maisVendidos = produtoQuery.topSellers(page, exibirIndisp);
        List<ProductCardVM> novidades    = produtoQuery.newArrivals(page, exibirIndisp);
        List<ProductCardVM> destaques    = produtoQuery.featured(page, exibirIndisp);
        List<ProductCardVM> paraVoce     = produtoQuery.recommended(page, exibirIndisp);
        if (paraVoce.isEmpty()) paraVoce = maisVendidos;

        return new HomePageVM(paraVoce, maisVendidos, novidades, destaques);
    }

    // ---- helpers ----
    private static int parseIntOrDefault(String v, int def) {
        if (v == null || v.isBlank()) return def;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}
