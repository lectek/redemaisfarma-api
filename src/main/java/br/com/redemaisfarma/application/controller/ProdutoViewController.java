/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageImpl
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestParam
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProdutoViewController {
    private final ProdutoRepository produtoRepository;

    public ProdutoViewController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping(value={"/produtos", "/catalogo"})
    public String listar(@RequestParam(value="q", required=false) String termo, @RequestParam(value="categoria", required=false) String categoria, @RequestParam(value="pagina", defaultValue="0") int pagina, @RequestParam(value="tamanho", defaultValue="12") int tamanho, Model model) {
        termo = termo == null ? "" : termo.trim();
        String string = categoria = categoria == null ? "" : categoria.trim();
        if (tamanho <= 0) {
            tamanho = 12;
        }
        if (pagina < 0) {
            pagina = 0;
        }
        PageRequest pageable = PageRequest.of((int)pagina, (int)tamanho, (Sort)Sort.by((String[])new String[]{"nome"}).ascending());
        Page<ProdutoEntity> page = !termo.isBlank() ? this.produtoRepository.searchPage(termo, (Pageable)pageable) : (!categoria.isBlank() ? this.produtoRepository.findByCategoriaIgnoreCase(categoria, (Pageable)pageable) : this.produtoRepository.findAll((Pageable)pageable));
        List<ProdutoView> produtos = page.getContent().stream().map(this::toView).toList();
        model.addAttribute("q", (Object)termo);
        model.addAttribute("categoriaSelecionada", (Object)categoria);
        model.addAttribute("categorias", this.produtoRepository.findDistinctCategorias());
        model.addAttribute("produtos", produtos);
        model.addAttribute("page", (Object)new PageImpl(produtos, (Pageable)pageable, page.getTotalElements()));
        return "pages/cliente/produtos";
    }

    private ProdutoView toView(ProdutoEntity e) {
        return new ProdutoView(e.getId(), e.getNome(), e.getCategoria(), e.getPrecoVenda(), e.getImagem());
    }

    public record ProdutoView(Long id, String nome, String categoria, BigDecimal precoVenda, String imagem) {
    }
}

