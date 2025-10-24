/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.BindingResult
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.redemaisfarma.application.controller.admin;

import br.com.redemaisfarma.application.view.admin.GatewayConfigForm;
import br.com.redemaisfarma.domain.financeiro.config.GatewayConfig;
import br.com.redemaisfarma.domain.financeiro.config.GatewayConfigService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value={"/admin/financeiro"})
public class FinanceiroGatewayAdminController {
    private static final String VIEW_PREFIX = "pages/admin/financeiro/";
    private final GatewayConfigService service;

    public FinanceiroGatewayAdminController(GatewayConfigService service) {
        this.service = service;
    }

    @GetMapping(value={"/gateways"})
    public String editar(@RequestParam(name="fornecedor") Optional<String> fornecedor, Model model) {
        String provedor = fornecedor.orElse("pagarme");
        GatewayConfigForm form = this.service.buscarAtivaPorProvedor(provedor).map(GatewayConfigForm::fromEntity).orElseGet(() -> {
            GatewayConfigForm f = new GatewayConfigForm();
            f.setProvedor(provedor);
            f.setAtivo(Boolean.TRUE);
            return f;
        });
        model.addAttribute("pageTitle", (Object)"Financeiro \u2022 Gateways");
        model.addAttribute("active", (Object)"financeiro");
        model.addAttribute("gw", (Object)form);
        return "pages/admin/financeiro/gateways";
    }

    @PostMapping(value={"/gateways"})
    public String salvar(@Valid @ModelAttribute(value="gw") GatewayConfigForm form, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", (Object)"Financeiro \u2022 Gateways");
            model.addAttribute("active", (Object)"financeiro");
            return "pages/admin/financeiro/gateways";
        }
        GatewayConfig toSave = form.toEntity();
        GatewayConfig saved = toSave.getId() == null ? this.service.criar(toSave) : this.service.atualizar(toSave.getId(), toSave);
        ra.addFlashAttribute("success", (Object)"Credenciais do gateway salvas com sucesso.");
        return "redirect:/admin/financeiro/gateways?fornecedor=" + saved.getProvedor();
    }
}

