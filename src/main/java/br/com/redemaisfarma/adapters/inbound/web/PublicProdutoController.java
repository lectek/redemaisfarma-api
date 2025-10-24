/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.domain.catalogo.ProdutoQueryService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/public/produtos"})
public class PublicProdutoController {
    private final ProdutoQueryService service;

    public PublicProdutoController(ProdutoQueryService service) {
        this.service = service;
    }

    @GetMapping(value={"/novidades"})
    public List<ProductCardVM> novidades(@RequestParam(defaultValue="12") int limit) {
        return this.service.newArrivals((Pageable)PageRequest.of((int)0, (int)Math.min(limit, 24)), false);
    }

    @GetMapping(value={"/para-voce"})
    public List<ProductCardVM> paraVoce(@RequestParam(defaultValue="12") int limit) {
        return this.service.recommended((Pageable)PageRequest.of((int)0, (int)Math.min(limit, 24)), false);
    }

    @GetMapping(value={"/mais-vendidos"})
    public List<ProductCardVM> maisVendidos(@RequestParam(defaultValue="12") int limit) {
        return this.service.topSellers((Pageable)PageRequest.of((int)0, (int)Math.min(limit, 24)), false);
    }
}

