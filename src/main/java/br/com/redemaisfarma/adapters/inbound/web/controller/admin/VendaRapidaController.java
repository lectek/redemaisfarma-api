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

import br.com.redemaisfarma.adapters.inbound.web.dto.ClienteLookupResponseDTO;
import br.com.redemaisfarma.adapters.inbound.web.dto.ProdutoBuscaDTO;
import br.com.redemaisfarma.adapters.inbound.web.dto.VendaRapidaFinalizarRequestDTO;
import br.com.redemaisfarma.adapters.inbound.web.dto.VendaRapidaFinalizarResponseDTO;
import br.com.redemaisfarma.application.service.CaixaVendaRapidaService;
import br.com.redemaisfarma.application.service.CaixaResumoService;
import br.com.redemaisfarma.application.service.VendaRapidaService;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Profile(value={"!test"})
@Controller
@RequestMapping(value={"/admin/vendas"})
public class VendaRapidaController {
    private final VendaRapidaService vendaRapida;
    private final CaixaVendaRapidaService caixaService;
    private final CaixaResumoService caixaResumoService;
    private final PedidoRepository pedidoRepo;
    private final ClienteRepository clienteRepo;

    @GetMapping("/rapida")
    public String page() {
        return "pages/admin/vendas/rapida";
    }

    @GetMapping("/rapida/produtos")
    @ResponseBody
    public List<ProdutoBuscaDTO> buscarProdutos(@RequestParam("q") String termo,
                                                @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return caixaService.buscarProdutos(termo, limit);
    }

    @GetMapping("/rapida/cliente")
    @ResponseBody
    public ClienteLookupResponseDTO buscarCliente(@RequestParam("cpf") String cpf) {
        String clean = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (clean.isBlank()) {
            return new ClienteLookupResponseDTO(false, null, null);
        }
        return clienteRepo.findByCpf(clean)
                .map(c -> new ClienteLookupResponseDTO(true, c.getNome(), c.getEmail()))
                .orElseGet(() -> new ClienteLookupResponseDTO(false, null, null));
    }

    @PostMapping("/rapida/finalizar")
    @ResponseBody
    public VendaRapidaFinalizarResponseDTO finalizar(@Valid @RequestBody VendaRapidaFinalizarRequestDTO request) {
        return caixaService.finalizar(request);
    }

    @GetMapping("/caixa/resumo")
    @ResponseBody
    public CaixaResumoService.CaixaResumo resumoCaixa(
            @RequestParam(value = "dia", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia
    ) {
        return caixaResumoService.resumoDia(dia);
    }

    @GetMapping("/rapida/recibo/{id}")
    public String recibo(@PathVariable Long id, Model model) {
        PedidoEntity pedido = pedidoRepo.buscarDetalheAdmin(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        model.addAttribute("pedido", pedido);
        return "pages/admin/vendas/recibo";
    }

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
    public VendaRapidaController(VendaRapidaService vendaRapida,
                                 CaixaVendaRapidaService caixaService,
                                 CaixaResumoService caixaResumoService,
                                 PedidoRepository pedidoRepo,
                                 ClienteRepository clienteRepo) {
        this.vendaRapida = vendaRapida;
        this.caixaService = caixaService;
        this.caixaResumoService = caixaResumoService;
        this.pedidoRepo = pedidoRepo;
        this.clienteRepo = clienteRepo;
    }
}
