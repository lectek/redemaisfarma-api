package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.produto.ProdutoVitrineService;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class HomePageController {

    /**
     * Default number of highlighted products.
     */
    private static final int DESTAQUES_LIMIT = 12;

    /**
     * Upper bound for page size.
     */
    private static final int MAX_PAGE_SIZE = 20;

    /**
     * Service that provides highlighted products.
     */
    private final ProdutoVitrineService vitrine;

    /**
     * Repository used to load available categories.
     */
    private final ProdutoRepository produtoRepository;

    /**
     * Creates controller with vitrine dependencies.
     *
     * @param service product showcase service
     * @param repository product repository
     */
    public HomePageController(
            final ProdutoVitrineService service,
            final ProdutoRepository repository
    ) {
        this.vitrine = service;
        this.produtoRepository = repository;
    }

    /**
     * Renders products page when no search query is present.
     *
     * @param model view model
     * @return products list page
     */
    @GetMapping(value = "/produtos", params = "!q")
    public String listarProdutos(final Model model) {
        final List<ProdutoEntity> loaded = vitrine.listarDestaques(
                DESTAQUES_LIMIT
        );
        final List<ProdutoEntity> destaques = loaded == null
                ? List.of()
                : loaded;
        final int pageSize = Math.max(
                1,
                Math.min(DESTAQUES_LIMIT, MAX_PAGE_SIZE)
        );
        final Pageable pageable = PageRequest.of(0, pageSize);
        final boolean hasDisponiveis = !destaques.isEmpty();

        model.addAttribute("destaques", destaques);
        model.addAttribute("lista", destaques);
        model.addAttribute("hasDisponiveis", hasDisponiveis);
        model.addAttribute("temProdutos", hasDisponiveis);
        model.addAttribute(
                "page",
                new PageImpl<>(destaques, pageable, destaques.size())
        );
        model.addAttribute("q", "");
        model.addAttribute("cat", "");
        model.addAttribute(
                "categorias",
                produtoRepository.findDistinctCategorias()
        );
        model.addAttribute("active", "produtos");

        return "pages/cliente/produtos/lista";
    }
}
