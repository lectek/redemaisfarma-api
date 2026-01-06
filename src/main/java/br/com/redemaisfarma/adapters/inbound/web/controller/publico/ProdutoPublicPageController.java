package br.com.redemaisfarma.adapters.inbound.web.controller.publico;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/produtos-publico")
public class ProdutoPublicPageController {

    /**
     * Catálogo público de produtos.
     * Usa o template produtos.html.
     * A própria página pode consumir a API /api/public/produtos via JS,
     * usando os parâmetros recebidos (categoria, tag, sort, page, size).
     */
    @GetMapping
    public String produtos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false, name = "sort") String ordenacao,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "24") Integer size,
            Model model) {

        model.addAttribute("categoriaSelecionada", categoria);
        model.addAttribute("tagSelecionada", tag);
        model.addAttribute("ordenacao", ordenacao);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // Thymeleaf: src/main/resources/templates/produtos.html
        return "produtos";
    }

    /**
     * Página alternativa de listagem (se você quiser usar lista.html para outra visão).
     * Ex.: /produtos/lista?categoria=MEDICAMENTOS
     */
    @GetMapping("/lista")
    public String lista(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String tag,
            Model model) {

        model.addAttribute("categoriaSelecionada", categoria);
        model.addAttribute("tagSelecionada", tag);

        // Thymeleaf: src/main/resources/templates/lista.html
        return "lista";
    }

    /**
     * Página de detalhes de um produto específico.
     * O template detalhes.html pode chamar /api/public/produtos/{id} via JS
     * para buscar os dados completos.
     */
    @GetMapping("/{id}")
    public String detalhes(@PathVariable("id") Long id, Model model) {
        model.addAttribute("produtoId", id);

        // Thymeleaf: src/main/resources/templates/detalhes.html
        return "detalhes";
    }
}
