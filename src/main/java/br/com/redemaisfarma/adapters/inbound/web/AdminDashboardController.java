/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 */
package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO;
import br.com.redemaisfarma.application.service.AdminMetricsService;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import java.util.ArrayList;
import java.util.Map;
import lombok.Generated;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {
    private final AdminMetricsService metricsService;

    @GetMapping(value={"/admin/dashboard"})
    public String dashboard(Model model) {
        PainelAdminResponseDTO painel = this.metricsService.montarPainel();
        model.addAttribute("painelAdmin", (Object)painel);
        Map<StatusPedido, Long> porStatus = painel.getPedidosPorStatus();
        ArrayList<String> chartLabels = new ArrayList<String>();
        ArrayList<Long> chartData = new ArrayList<Long>();
        if (porStatus != null && !porStatus.isEmpty()) {
            for (StatusPedido s : StatusPedido.values()) {
                if (!porStatus.containsKey((Object)s)) continue;
                chartLabels.add(s.name());
                chartData.add(porStatus.get((Object)s));
            }
        }
        model.addAttribute("chartStatusLabels", chartLabels);
        model.addAttribute("chartStatusData", chartData);
        return "pages/admin/dashboard";
    }

    @Generated
    public AdminDashboardController(AdminMetricsService metricsService) {
        this.metricsService = metricsService;
    }
}

