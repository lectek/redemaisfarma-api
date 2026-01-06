package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProdutoDetalheController {

    private final ProdutoRepository produtoRepository;

    public ProdutoDetalheController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping({"/produto/{id}", "/produtos/{id}"})
    public String detalhe(@PathVariable("id") Long id, Model model) {
        ProdutoEntity produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean outOfStock = produto.getEstoque() != null && produto.getEstoque() <= 0;
        model.addAttribute("produto", produto);
        model.addAttribute("outOfStock", outOfStock);
        return "pages/cliente/produtos/detalhe";
    }
}
