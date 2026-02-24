package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.service.ProdutoAdminService;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/produtos")
public class ProdutoAdminPageController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoAdminPageController.class);

    private final ProdutoAdminService adminService;
    private final ProdutoRepository produtoRepository;
    private final ProdutoCategoriaRepository categoriaRepository;

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
        model.addAttribute("categorias", this.resolveCategorias());
        return "pages/admin/produtos/lista";
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

    @PostMapping(consumes = "multipart/form-data")
    public String criarProduto(
            @ModelAttribute("produto") ProdutoEntity produto,
            @RequestParam(value = "imagemFile", required = false) MultipartFile imagemFile,
            RedirectAttributes ra
    ) {
        String nome = this.normalize(produto.getNome());
        if (nome.isBlank()) {
            return "redirect:/admin/produtos/novo?erro=nome";
        }
        produto.setNome(nome);

        String categoria = this.normalize(produto.getCategoria());
        if (categoria.isBlank()) {
            categoria = "Sem Categoria";
        }
        produto.setCategoria(categoria);

        String codigoBarras = this.normalize(produto.getCodigoBarras());
        if (codigoBarras.isBlank()) {
            produto.setCodigoBarras(null);
        } else {
            if (produtoRepository.existsByCodigoBarras(codigoBarras)) {
                return "redirect:/admin/produtos/novo?erro=codigo_barras";
            }
            produto.setCodigoBarras(codigoBarras);
        }

        log.debug("[admin-produto] criar | nome='{}', categoria='{}', codigoBarras='{}'",
                produto.getNome(), produto.getCategoria(), produto.getCodigoBarras());

        produto.setId(null);
        if (produto.getDisponivel() == null) {
            produto.setDisponivel(Boolean.TRUE);
        }
        if (produto.getDataCadastro() == null) {
            produto.setDataCadastro(LocalDate.now());
        }
        if (imagemFile != null && !imagemFile.isEmpty()) {
            String filename = StringUtils.cleanPath(imagemFile.getOriginalFilename());
            if (!filename.isBlank()) {
                produto.setImagem("/media/products/" + filename);
            }
        }
        ProdutoEntity salvo;
        try {
            salvo = produtoRepository.save(produto);
        } catch (DataIntegrityViolationException ex) {
            log.warn("[admin-produto] conflito integridade ao salvar | nome='{}', categoria='{}', codigoBarras='{}'",
                    produto.getNome(), produto.getCategoria(), produto.getCodigoBarras(), ex);
            return "redirect:/admin/produtos/novo?erro=integridade";
        }
        ra.addFlashAttribute("toast", "Produto criado com sucesso.");
        return "redirect:/admin/produtos/" + salvo.getId() + "/editar";
    }

    @GetMapping("/novo")
    public String novoProdutoPage(Model model) {
        model.addAttribute("produto", new ProdutoEntity());
        model.addAttribute("categorias", this.resolveCategorias());
        return "pages/admin/produtos/form";
    }

    @GetMapping(value = "/busca-rapida", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ProdutoLookupItem> buscaRapida(
            @RequestParam("q") String q,
            @RequestParam(defaultValue = "8") int limit
    ) {
        String termo = this.normalize(q);
        if (termo.isBlank()) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 20));
        Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.ASC, "nome"));
        return this.produtoRepository.searchPageByCategoria(termo, null, pageable)
                .getContent()
                .stream()
                .map(ProdutoLookupItem::from)
                .toList();
    }

    @GetMapping("/form")
    public String redirectFormToNovo() {
        return "redirect:/admin/produtos/novo";
    }

    @GetMapping("/{id}/editar")
    public String editarProdutoPage(@PathVariable Long id, Model model) {
        model.addAttribute("produtoId", id);
        return "pages/admin/produtos/editar";
    }

    private List<String> resolveCategorias() {
        List<String> categorias = this.categoriaRepository.findAllNomes();
        if (categorias == null || categorias.isEmpty()) {
            return List.of("Sem Categoria");
        }
        return categorias;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public static record ProdutoLookupItem(
            Long id,
            String nome,
            String descricao,
            String categoria,
            String codigoBarras,
            BigDecimal precoVenda,
            BigDecimal precoPromocional,
            Integer estoque,
            String fabricante,
            String unidade
    ) {
        static ProdutoLookupItem from(ProdutoEntity p) {
            return new ProdutoLookupItem(
                    p.getId(),
                    p.getNome(),
                    p.getDescricao(),
                    p.getCategoria(),
                    p.getCodigoBarras(),
                    p.getPrecoVenda(),
                    p.getPrecoPromocional(),
                    p.getEstoque(),
                    p.getFabricante(),
                    p.getUnidade()
            );
        }
    }

    @Generated
    public ProdutoAdminPageController(ProdutoAdminService adminService,
                                      ProdutoRepository produtoRepository,
                                      ProdutoCategoriaRepository categoriaRepository) {
        this.adminService = adminService;
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }
}



