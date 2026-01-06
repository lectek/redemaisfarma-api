// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/AdminSalesReportController.java
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.report.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
public class AdminSalesReportController {

    private final ReportService reportService;
    private final ClienteRepository clienteRepository;

    public AdminSalesReportController(ReportService reportService, ClienteRepository clienteRepository) {
        this.reportService = reportService;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/admin/relatorios/vendas")
    public String relatorioVendas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            Model model) {

        var resumo = reportService.listarVendasPorDia(de, ate);
        model.addAttribute("linhas", resumo.linhas());
        model.addAttribute("sumQtd", resumo.sumQtd());
        model.addAttribute("sumTotal", resumo.sumTotal());
        model.addAttribute("totalClientes", clienteRepository.count());

        return "pages/admin/relatorios/vendas";
    }

    // Export simples: CSV (já funciona); para "pdf", devolve 501 por enquanto.
    @GetMapping("/admin/relatorios/vendas/export")
    public ResponseEntity<byte[]> exportarVendas(
            @RequestParam(defaultValue = "csv") String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {

        var resumo = reportService.listarVendasPorDia(de, ate);

        if ("pdf".equalsIgnoreCase(tipo)) {
            String msg = "Exportação em PDF ainda não implementada. Use tipo=csv.";
            return ResponseEntity.status(501)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(msg.getBytes(StandardCharsets.UTF_8));
        }

        // CSV: data;qtd;total
        StringBuilder sb = new StringBuilder();
        sb.append("data;qtd;total\n");
        resumo.linhas().forEach(l ->
                sb.append(l.data()).append(';')
                  .append(l.qtd()).append(';')
                  .append(l.total()).append('\n')
        );
        sb.append("TOTAL;").append(resumo.sumQtd()).append(';').append(resumo.sumTotal()).append('\n');

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vendas.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }
}
