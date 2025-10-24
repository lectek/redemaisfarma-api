/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 */
package br.com.redemaisfarma.application.controller.admin;

import br.com.redemaisfarma.application.view.AssinaturaView;
import br.com.redemaisfarma.domain.financeiro.FinanceiroAdminService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value={"/admin/financeiro"})
public class FinanceiroAdminController {
    private static final Logger log = LoggerFactory.getLogger(FinanceiroAdminController.class);
    private static final String VIEW_PREFIX = "pages/admin/financeiro/";
    private final FinanceiroAdminService service;

    public FinanceiroAdminController(FinanceiroAdminService service) {
        this.service = service;
    }

    @GetMapping(value={"/assinaturas"})
    public String listar(Model model) {
        model.addAttribute("pageTitle", (Object)"Financeiro \u2022 Assinaturas");
        model.addAttribute("active", (Object)"financeiro");
        model.addAttribute("assinaturas", this.service.listarAssinaturas());
        return "pages/admin/financeiro/assinaturas";
    }

    @GetMapping(value={"/assinaturas/{id}"})
    public String detalhe(@PathVariable Long id, Model model) {
        Optional<AssinaturaView> assinatura = this.service.buscarAssinatura(id);
        if (assinatura.isEmpty()) {
            log.warn("Assinatura id={} n\u00e3o encontrada. Redirecionando para lista.", (Object)id);
            return "redirect:/admin/financeiro/assinaturas";
        }
        AssinaturaView a = assinatura.get();
        model.addAttribute("pageTitle", (Object)("Assinatura \u2022 " + a.clienteNome()));
        model.addAttribute("active", (Object)"financeiro");
        model.addAttribute("assinatura", (Object)a);
        return "pages/admin/financeiro/assinaturas-detalhe";
    }
}

