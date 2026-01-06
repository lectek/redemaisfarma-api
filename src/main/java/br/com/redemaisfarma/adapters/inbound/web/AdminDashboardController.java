// src/main/java/br/com/redemaisfarma/adapters/inbound/web/AdminDashboardController.java
package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.dto.response.AlertItemDTO;
import br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO;
import br.com.redemaisfarma.application.service.AdminMetricsService;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class AdminDashboardController {

    private final AdminMetricsService metricsService;
    private final ObjectProvider<PedidoRepository> pedidoRepository;

    public AdminDashboardController(AdminMetricsService metricsService,
                                    ObjectProvider<PedidoRepository> pedidoRepository) {
        this.metricsService = metricsService;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model,
                            @RequestParam(value = "period", required = false) String period) {
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

        model.addAttribute("period", period);
        model.addAttribute("ultimosPedidos", buscarUltimosPedidos(period));

        return "pages/admin/dashboard";
    }

    @GetMapping("/admin/alertas")
    public String alertas(Model model,
                          @RequestParam(value = "tipo", required = false) String tipo) {
        PainelAdminResponseDTO painel = metricsService.montarPainel();
        List<AlertItemDTO> alertas = painel.getAlertas() == null ? List.of() : painel.getAlertas();
        List<AlertItemDTO> filtrados = alertas;
        if (tipo != null && !tipo.isBlank()) {
            String filtro = tipo.trim();
            filtrados = alertas.stream()
                    .filter(a -> a.getTipo() != null && a.getTipo().equalsIgnoreCase(filtro))
                    .toList();
        }

        List<String> tiposDisponiveis = alertas.stream()
                .map(a -> a.getTipo() == null ? "" : a.getTipo().trim())
                .filter(t -> !t.isBlank())
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("painelAdmin", painel);
        model.addAttribute("alertasFiltrados", filtrados);
        model.addAttribute("tipoSelecionado", tipo == null ? "" : tipo.trim());
        model.addAttribute("tiposDisponiveis", tiposDisponiveis);
        return "pages/admin/alertas";
    }

    private List<PedidoResumoAdminView> buscarUltimosPedidos(String period) {
        PedidoRepository repo = pedidoRepository.getIfAvailable();
        if (repo == null) {
            return List.of();
        }
        LocalDateTime de = resolvePeriodo(period);
        List<PedidoEntity> pedidos = repo.listarRecentes(de, PageRequest.of(0, 5));
        if (pedidos.isEmpty()) {
            return List.of();
        }
        List<Long> ids = pedidos.stream()
                .map(PedidoEntity::getId)
                .filter(id -> id != null)
                .toList();
        Map<Long, Long> itensPorPedido = carregarItensPorPedido(repo, ids);
        return pedidos.stream()
                .map(p -> PedidoResumoAdminView.from(p, itensPorPedido.getOrDefault(p.getId(), 0L)))
                .toList();
    }

    private static LocalDateTime resolvePeriodo(String period) {
        if (period == null || period.isBlank()) {
            return null;
        }
        return switch (period) {
            case "today" -> LocalDate.now().atStartOfDay();
            case "week" -> LocalDateTime.now().minusDays(7);
            case "month" -> LocalDateTime.now().minusDays(30);
            default -> null;
        };
    }

    private static Map<Long, Long> carregarItensPorPedido(PedidoRepository repo, List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> map = new HashMap<>();
        for (var row : repo.contarItensPorPedidos(ids)) {
            if (row.getId() != null) {
                map.put(row.getId(), row.getTotalItens() != null ? row.getTotalItens() : 0L);
            }
        }
        return map;
    }

    private static String statusLabel(StatusPedido status) {
        if (status == null) {
            return "Desconhecido";
        }
        return switch (status) {
            case ABERTO -> "Aberto";
            case AGUARDANDO_PAGAMENTO -> "Aguardando pagamento";
            case PAGO -> "Pago";
            case ENVIADO -> "Enviado";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }

    private static String statusClass(StatusPedido status) {
        if (status == null) {
            return "badge--neutral";
        }
        return switch (status) {
            case ABERTO, AGUARDANDO_PAGAMENTO, ENVIADO -> "badge--warning";
            case CANCELADO -> "badge--danger";
            case PAGO, ENTREGUE -> "badge--success";
        };
    }

    public record PedidoResumoAdminView(
            Long id,
            String clienteNome,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String statusLabel,
            String statusClass,
            Long totalItens
    ) {
        static PedidoResumoAdminView from(PedidoEntity p, Long totalItens) {
            String cliente = p.getCliente() != null && p.getCliente().getNome() != null
                    ? p.getCliente().getNome()
                    : "Cliente";
            StatusPedido statusValue = p.getStatus();
            String status = statusValue != null ? statusValue.name() : "DESCONHECIDO";
            String label = AdminDashboardController.statusLabel(statusValue);
            String css = AdminDashboardController.statusClass(statusValue);
            long itens = totalItens != null ? totalItens : 0L;
            return new PedidoResumoAdminView(p.getId(), cliente, p.getData(), p.getTotal(), status, label, css, itens);
        }
    }
}
