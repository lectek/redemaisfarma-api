// src/main/java/br/com/redemaisfarma/adapters/inbound/web/AdminDashboardController.java
package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO;
import br.com.redemaisfarma.application.service.AdminMetricsService;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Map;

@Controller
public class AdminDashboardController {

    private final AdminMetricsService metricsService;

    public AdminDashboardController(AdminMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        PainelAdminResponseDTO painel = metricsService.montarPainel();
        model.addAttribute("painelAdmin", painel);

        Map<StatusPedido, Long> porStatus = painel.getPedidosPorStatus();
        var chartLabels = new ArrayList<String>();
        var chartData   = new ArrayList<Long>();

        if (porStatus != null && !porStatus.isEmpty()) {
            for (StatusPedido s : StatusPedido.values()) {
                if (porStatus.containsKey(s)) {
                    chartLabels.add(s.name());       // exemplo: PAGO, ENVIADO...
                    chartData.add(porStatus.get(s)); // contagem
                }
            }
        }

        model.addAttribute("chartStatusLabels", chartLabels);
        model.addAttribute("chartStatusData",   chartData);

        return "pages/admin/dashboard";
    }
}
