/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.application.core.produto.ProdutoVitrineService;
import java.util.List;
import lombok.Generated;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value={"/site"})
public class SiteHomeController {
    private final ProdutoVitrineService vitrine;

    @GetMapping(value={"", "/"})
    public String home(Model model) {
        List<ProdutoEntity> destaques = this.vitrine.listarDestaques(12);
        model.addAttribute("destaques", destaques);
        model.addAttribute("hasDisponiveis", (Object)(!destaques.isEmpty() ? 1 : 0));
        model.addAttribute("title", (Object)"RedeMaisFarma \u2022 Sua farm\u00e1cia de confian\u00e7a");
        return "pages/cliente/index";
    }

    @Generated
    public SiteHomeController(ProdutoVitrineService vitrine) {
        this.vitrine = vitrine;
    }
}

