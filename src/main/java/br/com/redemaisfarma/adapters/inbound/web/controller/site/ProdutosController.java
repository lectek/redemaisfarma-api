package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
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
@RequestMapping("/produtos")
public class ProdutosController {

    /**
     * Default page size for public product listing.
     */
    private static final int DEFAULT_PAGE_SIZE = 18;

    /**
     * Repository used to query public products.
     */
    private final ProdutoRepository repo;

    /**
     * Creates controller with product repository dependency.
     *
     * @param repository product repository
     */
    public ProdutosController(final ProdutoRepository repository) {
        this.repo = repository;
    }

    /**
     * Lists public products when search query parameter is present.
     *
     * @param q search term
     * @param cat category filter
     * @param pageable paging information
     * @param model view model
     * @return product list page
     */
    @GetMapping(params = "q")
    public String listPublic(
            @RequestParam("q") final String q,
            @RequestParam(value = "cat", required = false) final String cat,
            @PageableDefault(
                    size = DEFAULT_PAGE_SIZE,
                    sort = "dataCadastro",
                    direction = Sort.Direction.DESC
            ) final Pageable pageable,
            final Model model
    ) {
        final String termo = normalize(q);
        final String categoria = normalize(cat);

        final Page<ProdutoEntity> page = categoria == null
                ? repo.searchPublicPage(termo, pageable)
                : repo.searchPublicPageByCategoria(termo, categoria, pageable);

        model.addAttribute("page", page);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("cat", categoria == null ? "" : categoria);
        model.addAttribute("categorias", repo.findDistinctCategorias());
        return "pages/cliente/produtos/lista";
    }

    private static String normalize(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
