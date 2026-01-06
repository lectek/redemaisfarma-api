/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Profile(value={"!test"})
@Controller
@RequestMapping(value={"/admin/produtos"})
public class ProdutoAdminEditPageController {
    private final ProdutoRepository repo;
    private final ProdutoCategoriaRepository categoriaRepository;

    @GetMapping(value={"/{id}/editar/page"})
    public String editarView(@PathVariable Long id, Model model) {
        ProdutoEntity p = this.repo.findById(id).orElse(null);
        model.addAttribute("produto", (Object)p);
        model.addAttribute("produtoId", (Object)id);
        model.addAttribute("categorias", this.resolveCategorias());
        return "pages/admin/produtos/editar";
    }

    @PostMapping(value={"/{id}/imagem/regenerate"})
    public String regenerateImage(@PathVariable Long id, RedirectAttributes ra) {
        return this.repo.findById(id).map(ent -> {
            if (ent.getImagem() == null || ent.getImagem().isBlank()) {
                ent.setImagem("/images/placeholder.png");
                this.repo.save(ent);
                ra.addFlashAttribute("toast", (Object)"Imagem definida como placeholder.");
            } else {
                ra.addFlashAttribute("toast", (Object)"Produto j\u00e1 possui imagem.");
            }
            return "redirect:/admin/produtos/" + id + "/editar/page";
        }).orElseGet(() -> {
            ra.addFlashAttribute("toast", (Object)"Produto n\u00e3o encontrado.");
            return "redirect:/admin/produtos";
        });
    }

    @Generated
    public ProdutoAdminEditPageController(ProdutoRepository repo, ProdutoCategoriaRepository categoriaRepository) {
        this.repo = repo;
        this.categoriaRepository = categoriaRepository;
    }

    private List<String> resolveCategorias() {
        List<String> categorias = this.categoriaRepository.findAllNomes();
        if (categorias == null || categorias.isEmpty()) {
            return List.of("Sem Categoria");
        }
        return categorias;
    }
}
