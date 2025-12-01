// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/AdminReportController.java
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.report.service.ReportService;
import br.com.redemaisfarma.application.report.vm.ClienteRelatorioVM;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Endpoint para a página consumir via fetch/Ajax
    @GetMapping(value = "/admin/relatorios/clientes/dados", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClienteRelatorioVM>> relatorioClientesDados() {
        List<ClienteRelatorioVM> clientes = reportService.listarClienteResumo();
        return ResponseEntity.ok(clientes);
    }

    // Endpoint para abrir/baixar o PDF
    @GetMapping(value = "/admin/relatorios/clientes.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> relatorioClientesPdf() {
        // Implemente este método no ReportService
        // ex.: byte[] pdf = reportService.gerarRelatorioClientesPdf();
        byte[] pdf = reportService.gerarRelatorioClientesPdf();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-clientes.pdf")
                .body(new ByteArrayResource(pdf));
    }
}
