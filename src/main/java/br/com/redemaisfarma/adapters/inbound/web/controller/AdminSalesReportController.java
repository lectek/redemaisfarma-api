package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.report.service.ReportService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminSalesReportController {

    /**
     * Fallback message for unsupported PDF export.
     */
    private static final String PDF_NOT_IMPLEMENTED_MESSAGE =
            "Exportacao em PDF ainda nao implementada. Use tipo=csv.";

    /**
     * Service used to build report data.
     */
    private final ReportService reportService;

    /**
     * Repository used for customer counters.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Creates controller with report dependencies.
     *
     * @param service report service
     * @param repository customer repository
     */
    public AdminSalesReportController(
            final ReportService service,
            final ClienteRepository repository
    ) {
        this.reportService = service;
        this.clienteRepository = repository;
    }

    /**
     * Renders sales report page.
     *
     * @param de start date
     * @param ate end date
     * @param model thymeleaf model
     * @return report view
     */
    @GetMapping("/admin/relatorios/vendas")
    public String relatorioVendas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate ate,
            final Model model
    ) {
        final var resumo = reportService.listarVendasPorDia(de, ate);
        model.addAttribute("linhas", resumo.linhas());
        model.addAttribute("sumQtd", resumo.sumQtd());
        model.addAttribute("sumTotal", resumo.sumTotal());
        model.addAttribute("totalClientes", clienteRepository.count());
        return "pages/admin/relatorios/vendas";
    }

    /**
     * Exports sales report in CSV. PDF is not implemented yet.
     *
     * @param tipo export type
     * @param de start date
     * @param ate end date
     * @return exported file payload
     */
    @GetMapping("/admin/relatorios/vendas/export")
    public ResponseEntity<byte[]> exportarVendas(
            @RequestParam(defaultValue = "csv") final String tipo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate ate
    ) {
        final var resumo = reportService.listarVendasPorDia(de, ate);

        if ("pdf".equalsIgnoreCase(tipo)) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(PDF_NOT_IMPLEMENTED_MESSAGE.getBytes(
                            StandardCharsets.UTF_8
                    ));
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("data;qtd;total").append('\n');
        resumo.linhas().forEach(linha ->
                sb.append(linha.data()).append(';')
                        .append(linha.qtd()).append(';')
                        .append(linha.total()).append('\n')
        );
        sb.append("TOTAL;")
                .append(resumo.sumQtd())
                .append(';')
                .append(resumo.sumTotal())
                .append('\n');

        final byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        final MediaType csvMediaType = new MediaType(
                "text",
                "csv",
                StandardCharsets.UTF_8
        );
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=vendas.csv"
                )
                .contentType(csvMediaType)
                .body(bytes);
    }
}
