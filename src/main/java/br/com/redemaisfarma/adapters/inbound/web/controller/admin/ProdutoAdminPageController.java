/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.context.annotation.Profile
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.data.web.PageableDefault
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.service.ProdutoAdminService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Profile(value={"!test"})
@Controller
@RequestMapping(value={"/admin/produtos"})
public class ProdutoAdminPageController {
    private final ProdutoAdminService adminService;
    private final ProdutoRepository produtoRepository;

    @GetMapping
    public String list(@RequestParam(required=false) String q, @RequestParam(required=false) String categoria, @PageableDefault(size=20, sort={"id"}, direction=Sort.Direction.ASC) Pageable pageable, Model model) {
        Page<ProdutoEntity> page = this.adminService.buscarPagina(q, categoria, pageable);
        model.addAttribute("page", page);
        model.addAttribute("produtos", (Object)page.getContent());
        model.addAttribute("q", (Object)(q == null ? "" : q));
        model.addAttribute("categoria", (Object)(categoria == null ? "" : categoria));
        model.addAttribute("categorias", this.produtoRepository.findDistinctCategorias());
        return "admin/produtos/lista";
    }

    @GetMapping(value={"/export.csv"}, produces={"text/csv"})
    public ResponseEntity<StreamingResponseBody> export(@RequestParam(required=false) String q, @RequestParam(required=false) String categoria, @RequestParam(defaultValue="2000") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50000));
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String filename = "produtos-" + ts + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        StreamingResponseBody body = os -> this.adminService.writeCsv(os, q, categoria, safeLimit);
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded})).contentType(MediaType.parseMediaType((String)"text/csv; charset=UTF-8")).body((Object)body);
    }

    @GetMapping(value={"/novo"})
    public String novoProdutoPage() {
        return "admin/produtos/novo";
    }

    @GetMapping(value={"/{id}/editar"})
    public String editarProdutoPage(@PathVariable Long id, Model model) {
        model.addAttribute("produtoId", (Object)id);
        return "pages/admin/produtos/editar";
    }

    @Generated
    public ProdutoAdminPageController(ProdutoAdminService adminService, ProdutoRepository produtoRepository) {
        this.adminService = adminService;
        this.produtoRepository = produtoRepository;
    }
}

