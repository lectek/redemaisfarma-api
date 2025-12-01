// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/AdminProductReportController.java
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.report.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class AdminProductReportController {

    private final ReportService reportService;

    public AdminProductReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/admin/relatorios/produtos")
    public String relatorioProdutos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            Model model) {

        if (de == null && ate == null) {
            model.addAttribute("linhas", reportService.listarProdutoResumo());
        } else {
            LocalDateTime ini = (de  != null) ? de.atStartOfDay() : null;
            LocalDateTime fim = (ate != null) ? ate.plusDays(1).atStartOfDay() : null;
            model.addAttribute("linhas", reportService.listarProdutoResumo(ini, fim));
        }
        return "admin/relatorios/produtos";
    }
}
