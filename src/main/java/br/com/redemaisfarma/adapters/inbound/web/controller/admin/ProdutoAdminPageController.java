package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.service.ProdutoAdminService;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Profile("!test")
@Controller
@RequestMapping("/admin/produtos")
public class ProdutoAdminPageController {

    private final ProdutoAdminService adminService;
    private final ProdutoRepository produtoRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        Page<ProdutoEntity> page = adminService.buscarPagina(q, categoria, pageable);
        model.addAttribute("page", page);
        model.addAttribute("produtos", page.getContent());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("categoria", categoria == null ? "" : categoria);
        model.addAttribute("categorias", produtoRepository.findDistinctCategorias());
        return "admin/produtos/lista";
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "2000") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 50_000));
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String filename = "produtos-" + ts + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        // lambda sem casts para preservar o tipo genérico
        StreamingResponseBody body = os -> {
            try {
                adminService.writeCsv(os, q, categoria, safeLimit);
            } catch (Exception e) { // se writeCsv lançar checked
                throw new RuntimeException("Falha ao gerar CSV", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    @GetMapping("/novo")
    public String novoProdutoPage() {
        return "admin/produtos/novo";
    }

    @GetMapping("/{id}/editar")
    public String editarProdutoPage(@PathVariable Long id, Model model) {
        model.addAttribute("produtoId", id);
        return "pages/admin/produtos/editar";
    }

    @Generated
    public ProdutoAdminPageController(ProdutoAdminService adminService, ProdutoRepository produtoRepository) {
        this.adminService = adminService;
        this.produtoRepository = produtoRepository;
    }
}
