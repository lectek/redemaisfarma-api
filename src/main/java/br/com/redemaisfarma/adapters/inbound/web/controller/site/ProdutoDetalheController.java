package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.TarjaMedicacao;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProdutoDetalheController {

    /**
     * Repository used to load product detail.
     */
    private final ProdutoRepository produtoRepository;

    /**
     * Creates controller for product detail.
     *
     * @param repository product repository
     */
    public ProdutoDetalheController(final ProdutoRepository repository) {
        this.produtoRepository = repository;
    }

    /**
     * Renders customer product detail page.
     *
     * @param id product id
     * @param model view model
     * @return detail page template
     */
    @GetMapping({"/produto/{id}", "/produtos/{id}"})
    public String detalhe(
            @PathVariable("id") final Long id,
            final Model model
    ) {
        final ProdutoEntity produto = produtoRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
                );
        final boolean outOfStock = produto.getEstoque() != null
                && produto.getEstoque() <= 0;
        final boolean exigeReceita = Boolean.TRUE.equals(produto.getExigeReceita());
        final TarjaMedicacao tarjaMedicacao = produto.getTarjaMedicacao();
        model.addAttribute("produto", produto);
        model.addAttribute("outOfStock", outOfStock);
        model.addAttribute("exigeReceita", exigeReceita);
        model.addAttribute("tarjaMedicacao", tarjaMedicacao);
        model.addAttribute("tarjaDescricao",
                tarjaMedicacao == null ? null : tarjaMedicacao.getDescricaoRegulatoria());
        return "pages/cliente/produtos/detalhe";
    }
}
