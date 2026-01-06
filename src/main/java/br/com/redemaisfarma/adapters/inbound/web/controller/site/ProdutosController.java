/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.data.web.PageableDefault
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value={"/produtos"})
public class ProdutosController {
    private final ProdutoRepository repo;

    @GetMapping(params={"q"})
    public String listPublic(@RequestParam String q,
                             @RequestParam(required=false) String cat,
                             @PageableDefault(size=18, sort={"dataCadastro"}, direction=Sort.Direction.DESC) Pageable pageable,
                             Model model) {
        String termo = q == null || q.isBlank() ? null : q.trim();
        String categoria = cat == null || cat.isBlank() ? null : cat.trim();
        Page<ProdutoEntity> page = categoria == null
                ? this.repo.searchPublicPage(termo, pageable)
                : this.repo.searchPublicPageByCategoria(termo, categoria, pageable);
        model.addAttribute("page", page);
        model.addAttribute("q", (Object)(q == null ? "" : q));
        model.addAttribute("cat", (Object)(categoria == null ? "" : categoria));
        model.addAttribute("categorias", this.repo.findDistinctCategorias());
        return "pages/cliente/produtos/lista";
    }

    @Generated
    public ProdutosController(ProdutoRepository repo) {
        this.repo = repo;
    }
}
