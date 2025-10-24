/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.domain.catalogo.ProdutoQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/public/carrossel"}, produces={"application/json"})
public class PublicCarrosselController {
    private final ProdutoQueryService service;

    public PublicCarrosselController(ProdutoQueryService service) {
        this.service = service;
    }

    @GetMapping(value={"/destaques"})
    public List<ProductCardVM> destaques(@RequestParam(defaultValue="10") int limit) {
        int safe = Math.min(Math.max(limit, 1), 20);
        return this.service.featured(safe);
    }
}

