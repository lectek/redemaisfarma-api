/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Controller
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.service.VendaRapidaService;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Profile(value={"!test"})
@Controller
@RequestMapping(value={"/admin/vendas"})
public class VendaRapidaController {
    private final VendaRapidaService vendaRapida;

    @PostMapping(value={"/rapida"})
    public String vender(@RequestParam(value="cliente") String refCliente, @RequestParam(value="produto") String refProduto, @RequestParam(value="qtd") int quantidade, RedirectAttributes ra) {
        try {
            Long pedidoId = this.vendaRapida.criar(refCliente, refProduto, quantidade);
            ra.addFlashAttribute("ok", (Object)"Venda registrada com sucesso.");
            return "redirect:/admin/pedidos/" + pedidoId;
        }
        catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", (Object)e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @Generated
    public VendaRapidaController(VendaRapidaService vendaRapida) {
        this.vendaRapida = vendaRapida;
    }
}

