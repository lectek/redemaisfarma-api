/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestParam
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.view.HomePageVM;
import br.com.redemaisfarma.domain.catalogo.HomepageCatalogFacade;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final HomepageCatalogFacade facade;

    @GetMapping(value={"/home"})
    public String exibirPaginaInicial(@RequestParam(value="onboarding", required=false) String onboarding, Model model) {
        HomePageVM vm = null;
        try {
            vm = this.facade.buildHomepage();
            log.debug("HomePageVM carregada? {}", (Object)(vm != null ? 1 : 0));
        }
        catch (Exception e) {
            log.error("Falha ao montar HomePageVM", (Throwable)e);
        }
        model.addAttribute("vm", (Object)vm);
        model.addAttribute("showWelcome", (Object)this.isOnboarding(onboarding));
        model.addAttribute("page", (Object)"home");
        return "pages/cliente/index";
    }

    @GetMapping(value={"/cadastro"})
    public String redirectCadastro() {
        return "redirect:/clientes/cadastro";
    }

    @GetMapping(value={"/cadastro-cliente"})
    public String redirectCadastroClienteAntigo() {
        return "redirect:/clientes/cadastro";
    }

    @GetMapping(value={"/checkout"})
    public String exibirCheckout(Model model) {
        model.addAttribute("page", (Object)"checkout");
        return "pages/cliente/checkout";
    }

    @GetMapping(value={"/carrinho"})
    public String exibirCarrinho(Model model) {
        model.addAttribute("page", (Object)"carrinho");
        return "pages/cliente/carrinho";
    }

    @GetMapping(value={"/sobre"})
    public String exibirSobre(Model model) {
        model.addAttribute("page", (Object)"sobre");
        return "pages/cliente/sobre";
    }

    private boolean isOnboarding(String v) {
        if (v == null) {
            return false;
        }
        return "1".equals(v) || "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
    }

    @Generated
    public HomeController(HomepageCatalogFacade facade) {
        this.facade = facade;
    }
}

