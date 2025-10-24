/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.application.core.produto.ProdutoVitrineService;
import java.util.List;
import lombok.Generated;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {
    private final ProdutoVitrineService vitrine;

    @GetMapping(value={"/produtos"}, params={"!q"})
    public String index(Model model) {
        List<ProdutoEntity> destaques = this.vitrine.listarDestaques(12);
        boolean hasDisponiveis = destaques != null && !destaques.isEmpty();
        model.addAttribute("destaques", destaques);
        model.addAttribute("hasDisponiveis", (Object)hasDisponiveis);
        model.addAttribute("lista", destaques);
        model.addAttribute("temProdutos", (Object)hasDisponiveis);
        return "pages/cliente/index";
    }

    @Generated
    public HomePageController(ProdutoVitrineService vitrine) {
        this.vitrine = vitrine;
    }
}

