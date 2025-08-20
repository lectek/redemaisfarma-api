package br.com.redemaisfarma.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador responsÃƒÂ¡vel por exibir o painel administrativo da loja de dropshipping. Mostra informaÃƒÂ§ÃƒÂµes de desempenho,
 * relatÃƒÂ³rios e atalhos para gestÃƒÂ£o de pedidos, produtos e clientes.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    /**
     * Rota principal do painel. Simula dados que serÃƒÂ£o preenchidos com valores reais no futuro.
     *
     * @param model
     *            modelo do Thymeleaf para repassar dados ÃƒÂ  view
     *
     * @return template HTML do painel
     */
    @GetMapping("/painel")
    public String exibirPainelAdmin(Model model) {

        // Simula dados do painel
        PainelAdminResponseDTO painel = new PainelAdminResponseDTO();
        painel.setAdminNome("Alex Morais");
        painel.setTotalPedidos(148);
        painel.setTotalLucro(18452.75);
        painel.setTicketMedio(124.75);
        painel.setClientesAtivos(89);
        painel.setCategoriasMaisVendidas(List.of("Coleiras", "RaÃƒÂ§ÃƒÂµes", "Brinquedos"));
        painel.setDataUltimoPedido(LocalDateTime.now().minusHours(2));
        painel.setQtdProdutos(76);
        painel.setQtdPedidosPendentes(12);
        painel.setQtdPedidosEntregues(121);
        painel.setSatisfacaoCliente(92.3);
        painel.setAlertas(List.of("12 pedidos aguardando envio", "3 produtos com estoque abaixo de 5 unidades",
                "Novo cliente VIP cadastrado hoje"));

        model.addAttribute("painelAdmin", painel);
        return "pages/painel";
    }

    /**
     * Rota alternativa para redirecionamento rÃƒÂ¡pido. Pode ser usada futuramente para exibir painel reduzido no mobile.
     */
    @GetMapping
    public String redirecionarParaPainel() {
        return "redirect:/admin/painel";
    }
}
