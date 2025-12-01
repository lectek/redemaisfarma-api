package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class ProdutoViewController {

    private final ProdutoRepository produtoRepository;

    public ProdutoViewController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping({"/produtos", "/catalogo"})
    public String listar(@RequestParam(value = "q", required = false) String termo,
                         @RequestParam(value = "categoria", required = false) String categoria,
                         @RequestParam(value = "pagina", defaultValue = "0") int pagina,
                         @RequestParam(value = "tamanho", defaultValue = "12") int tamanho,
                         Model model) {

        final String q = (termo == null ? "" : termo.trim());
        final String cat = (categoria == null ? "" : categoria.trim());

        if (tamanho <= 0) tamanho = 12;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());

        Page<ProdutoEntity> page =
                !q.isBlank() ? produtoRepository.searchPage(q, pageable)
                        : (!cat.isBlank() ? produtoRepository.findByCategoriaIgnoreCase(cat, pageable)
                                          : produtoRepository.findAll(pageable));

        List<ProdutoView> produtos = page.getContent().stream()
                .map(this::toView)
                .toList();

        model.addAttribute("q", q);
        model.addAttribute("categoriaSelecionada", cat);
        model.addAttribute("categorias", produtoRepository.findDistinctCategorias());
        model.addAttribute("produtos", produtos);
        // PageImpl parametrizado para evitar raw type warning
        model.addAttribute("page", new PageImpl<>(produtos, pageable, page.getTotalElements()));

        return "pages/cliente/produtos";
    }

    private ProdutoView toView(ProdutoEntity e) {
        return new ProdutoView(e.getId(), e.getNome(), e.getCategoria(), e.getPrecoVenda(), e.getImagem());
    }

    public record ProdutoView(Long id, String nome, String categoria, BigDecimal precoVenda, String imagem) { }
}
