package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.service.EstoqueFisicoCsvService;
import br.com.redemaisfarma.application.service.EstoqueFisicoImportService;
import br.com.redemaisfarma.application.service.ProdutoAdminService;
import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
import br.com.redemaisfarma.domain.support.BarcodeNormalizer;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/produtos")
public class ProdutoAdminPageController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoAdminPageController.class);

    private final ProdutoAdminService adminService;
    private final ProdutoRepository produtoRepository;
    private final ProdutoCategoriaRepository categoriaRepository;
    private final ImageStorageService imageStorageService;
    private final EstoqueFisicoCsvService estoqueFisicoCsvService;
    private final ObjectProvider<EstoqueFisicoImportService> estoqueImportServiceProvider;
    private final ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider;
    private final ObjectProvider<SincronizacaoCatalogoService> catalogSyncProvider;

    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        Page<ProdutoEntity> page = adminService.buscarPagina(q, categoria, pageable);
        model.addAttribute("page", page);
        model.addAttribute("produtos", page.getContent());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("categoria", categoria == null ? "" : categoria);
        model.addAttribute("categorias", this.resolveCategorias());
        model.addAttribute("legacySyncEnabled", this.catalogSyncProvider.getIfAvailable() != null);
        return "pages/admin/produtos/lista";
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "limit", defaultValue = "2000") int limit
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

        if (produto.getLegacyId() != null && produto.getLegacyId() <= 0) {
            produto.setLegacyId(null);
        }

        if (produto.getLegacyId() != null) {
            Optional<ProdutoEntity> existente = produtoRepository.findByLegacyId(produto.getLegacyId());
            if (existente.isPresent()) {
                ra.addFlashAttribute("info", "Produto ja existe no catalogo. Abrindo edicao.");
                return "redirect:/admin/produtos/" + existente.get().getId() + "/editar";
            }
        }

        String codigoBarras = this.normalize(produto.getCodigoBarras());
        if (codigoBarras.isBlank()) {
            produto.setCodigoBarras(null);
        } else {
            String barcodeNormalizado = this.normalizeBarcode(codigoBarras);
            Optional<ProdutoEntity> existentePorCodigo = produtoRepository.findByCodigoBarras(barcodeNormalizado);
            if (existentePorCodigo.isPresent()) {
                ra.addFlashAttribute("info", "Codigo de barras ja cadastrado. Abrindo edicao.");
                return "redirect:/admin/produtos/" + existentePorCodigo.get().getId() + "/editar";
            }
            produto.setCodigoBarras(barcodeNormalizado);
        }

        log.debug("[admin-produto] criar | nome='{}', categoria='{}', codigoBarras='{}'",
                produto.getNome(), produto.getCategoria(), produto.getCodigoBarras());

        if (imagemFile == null || imagemFile.isEmpty()) {
            return "redirect:/admin/produtos/novo?erro=imagem";
        }

        produto.setId(null);
        if (produto.getDisponivel() == null) {
            produto.setDisponivel(Boolean.TRUE);
        }
        if (produto.getDataCadastro() == null) {
            produto.setDataCadastro(LocalDate.now());
        }
        if (produto.getMetodoLeituraCodigoBarras() == null) {
            produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        }
        produto.setImagem(null);
        produto.setStatus(ProdutoStatus.IMPORTADO);
        produto.setPublicadoEm(null);

        ProdutoEntity salvo;
        try {
            salvo = produtoRepository.save(produto);
        } catch (DataIntegrityViolationException ex) {
            log.warn("[admin-produto] conflito integridade ao salvar | nome='{}', categoria='{}', codigoBarras='{}'",
                    produto.getNome(), produto.getCategoria(), produto.getCodigoBarras(), ex);
            return "redirect:/admin/produtos/novo?erro=integridade";
        }

        try {
            String imageUrl = imageStorageService.saveProductImage(salvo.getId(), imagemFile);
            salvo.setImagem(imageUrl);
        } catch (IOException ex) {
            log.warn("[admin-produto] falha upload imagem id={} nome='{}': {}", salvo.getId(), salvo.getNome(), ex.getMessage());
            produtoRepository.deleteById(salvo.getId());
            return "redirect:/admin/produtos/novo?erro=imagem_upload";
        }

        salvo.setStatus(ProdutoStatus.IMPORTADO);
        salvo.setPublicadoEm(null);
        produtoRepository.save(salvo);

        ra.addFlashAttribute("success", "Produto criado em IMPORTADO. Valide e publique na edicao.");
        return "redirect:/admin/produtos/" + salvo.getId() + "/editar";
    }

    @GetMapping("/novo")
    public String novoProdutoPage(Model model) {
        model.addAttribute("produto", new ProdutoEntity());
        model.addAttribute("categorias", this.resolveCategorias());
        model.addAttribute("legacySyncEnabled", this.catalogSyncProvider.getIfAvailable() != null);
        return "pages/admin/produtos/form";
    }

    @GetMapping(value = "/busca-rapida", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ProdutoLookupItem> buscaRapida(
            @RequestParam("q") String q,
            @RequestParam(name = "limit", defaultValue = "8") int limit
    ) {
        String termo = this.normalize(q);
        if (termo.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 20));
        Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.ASC, "nome"));

        List<ProdutoLookupItem> itens = new ArrayList<>(safeLimit);
        this.produtoRepository.searchPageByCategoria(termo, null, pageable)
                .getContent()
                .stream()
                .map(ProdutoLookupItem::from)
                .forEach(itens::add);

        // Mantem a busca util mesmo quando o termo nao casa com nenhum nome.
        // Nesse caso, sugere itens recentes do catalogo para selecao rapida.
        if (itens.isEmpty()) {
            Pageable sugestoesPage = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "id"));
            this.produtoRepository.findAll(sugestoesPage)
                    .getContent()
                    .stream()
                    .map(ProdutoLookupItem::from)
                    .forEach(itens::add);
        }

        if (itens.size() < safeLimit) {
            ProdutoLegacyRepository legacyRepository = this.legacyRepositoryProvider.getIfAvailable();
            if (legacyRepository != null) {
                List<ProdutoLegacyEntity> legacyMatches = this.buscarNoEstoqueFisico(legacyRepository, termo, safeLimit);
                for (ProdutoLegacyEntity legacy : legacyMatches) {
                    if (itens.size() >= safeLimit) {
                        break;
                    }

                    ProdutoLookupItem mapped = ProdutoLookupItem.fromLegacy(legacy);
                    if (mapped.legacyId() != null && this.produtoRepository.existsByLegacyId(mapped.legacyId())) {
                        continue;
                    }
                    if (StringUtils.hasText(mapped.codigoBarras()) && this.produtoRepository.existsByCodigoBarras(mapped.codigoBarras())) {
                        continue;
                    }

                    boolean duplicateByLegacy = mapped.legacyId() != null && itens.stream()
                            .anyMatch(existing -> mapped.legacyId().equals(existing.legacyId()));
                    boolean duplicateByBarcode = StringUtils.hasText(mapped.codigoBarras()) && itens.stream()
                            .anyMatch(existing -> mapped.codigoBarras().equals(existing.codigoBarras()));

                    if (duplicateByLegacy || duplicateByBarcode) {
                        continue;
                    }
                    itens.add(mapped);
                }
            }
        }

        return itens.stream()
                .sorted(Comparator.comparing(item -> this.normalize(item.nome())))
                .limit(safeLimit)
                .toList();
    }

    @GetMapping(value = "/nao-prontos", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProdutoLookupPage listarNaoProntos(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 40));
        List<ProdutoLookupItem> naoProntos = this.resolveNaoProntos(q);

        int from = safePage * safeSize;
        if (from >= naoProntos.size()) {
            return new ProdutoLookupPage(List.of(), safePage, safeSize, naoProntos.size(), false);
        }
        int to = Math.min(from + safeSize, naoProntos.size());
        List<ProdutoLookupItem> content = naoProntos.subList(from, to);
        boolean hasNext = to < naoProntos.size();

        return new ProdutoLookupPage(content, safePage, safeSize, naoProntos.size(), hasNext);
    }

    @GetMapping("/nao-prontos/todos")
    public String listarNaoProntosTodos(@RequestParam(name = "q", required = false) String q, Model model) {
        List<ProdutoLookupItem> naoProntos = this.resolveNaoProntos(q);
        model.addAttribute("pendingItems", naoProntos);
        model.addAttribute("pendingTotal", naoProntos.size());
        model.addAttribute("q", q == null ? "" : q.trim());
        return "pages/admin/produtos/nao-prontos-todos";
    }

    @PostMapping("/sincronizar-estoque")
    public String sincronizarEstoqueFisico(RedirectAttributes ra) {
        SincronizacaoCatalogoService syncService = this.catalogSyncProvider.getIfAvailable();
        if (syncService == null) {
            ra.addFlashAttribute("warning", "Sincronizacao indisponivel. Ative legacy.sync.enabled e configure Firebird.");
            return "redirect:/admin/produtos";
        }

        try {
            SincronizacaoCatalogoService.ResumoSync resumo = syncService.sincronizarTudo();
            ra.addFlashAttribute("success",
                    "Sincronizacao concluida. Lidos: " + resumo.lidos()
                            + ", inseridos: " + resumo.inseridos()
                            + ", atualizados: " + resumo.atualizados()
                            + ", ignorados: " + resumo.ignorados()
                            + ", erros: " + resumo.erros() + ".");
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao sincronizar estoque fisico", ex);
            ra.addFlashAttribute("error", "Falha ao sincronizar estoque fisico. Verifique as configuracoes do legado.");
        }
        return "redirect:/admin/produtos";
    }

    @PostMapping("/importar-estoque-fisico")
    public String importarEstoqueFisico(RedirectAttributes ra) {
        EstoqueFisicoImportService importService = this.estoqueImportServiceProvider.getIfAvailable();
        if (importService == null) {
            ra.addFlashAttribute("warning", "Importacao CSV indisponivel no ambiente atual.");
            return "redirect:/admin/produtos/novo";
        }

        try {
            EstoqueFisicoImportService.ImportacaoResumo resumo = importService.importarTodosComoNaoDisponiveis();
            ra.addFlashAttribute("success",
                    "Importacao concluida: lidos " + resumo.lidos()
                            + ", inseridos " + resumo.inseridos()
                            + ", atualizados " + resumo.atualizados()
                            + ", ignorados " + resumo.ignorados()
                            + ", erros " + resumo.erros() + ".");
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao importar estoque fisico CSV", ex);
            ra.addFlashAttribute("error", "Falha ao importar estoque fisico. Verifique o arquivo CSV.");
        }
        return "redirect:/admin/produtos/novo";
    }

    @GetMapping("/form")
    public String redirectFormToNovo() {
        return "redirect:/admin/produtos/novo";
    }

    @GetMapping("/{id}/editar")
    public String editarProdutoPage(@PathVariable("id") Long id, Model model) {
        model.addAttribute("produtoId", id);
        this.produtoRepository.findById(id).ifPresent(produto -> model.addAttribute("produto", produto));
        model.addAttribute("categorias", this.resolveCategorias());
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

    private String normalizeBarcode(String value) {
        return BarcodeNormalizer.normalize(value);
    }

    private boolean isPositivePrice(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isReadyForSale(ProdutoEntity produto) {
        if (produto == null) {
            return false;
        }
        return produto.getStatus() == ProdutoStatus.PUBLICADO
                && Boolean.TRUE.equals(produto.getDisponivel())
                && this.isPositivePrice(produto.getPrecoVenda())
                && StringUtils.hasText(produto.getImagem())
                && produto.getEstoque() != null
                && produto.getEstoque() > 0;
    }

    private ProdutoEntity findFirstMatch(
            EstoqueFisicoCsvService.EstoqueItem stock,
            LinkedHashMap<Long, ProdutoEntity> byLegacy,
            LinkedHashMap<String, ProdutoEntity> byBarcode
    ) {
        if (stock.legacyId() != null) {
            ProdutoEntity byLegacyId = byLegacy.get(stock.legacyId());
            if (byLegacyId != null) {
                return byLegacyId;
            }
        }
        if (StringUtils.hasText(stock.codigoBarras())) {
            ProdutoEntity byBar = byBarcode.get(this.normalizeBarcode(stock.codigoBarras()));
            if (byBar != null) {
                return byBar;
            }
        }
        return null;
    }

    private List<ProdutoLookupItem> resolveNaoProntos(String q) {
        List<EstoqueFisicoCsvService.EstoqueItem> base = this.estoqueFisicoCsvService.search(q);
        if (base == null || base.isEmpty()) {
            return List.of();
        }

        List<ProdutoEntity> catalogo = this.produtoRepository.findAll();
        LinkedHashMap<Long, ProdutoEntity> byLegacy = new LinkedHashMap<>();
        LinkedHashMap<String, ProdutoEntity> byBarcode = new LinkedHashMap<>();
        for (ProdutoEntity produto : catalogo) {
            if (produto == null) {
                continue;
            }
            if (produto.getLegacyId() != null) {
                byLegacy.putIfAbsent(produto.getLegacyId(), produto);
            }
            String barcode = this.normalizeBarcode(produto.getCodigoBarras());
            if (!barcode.isBlank()) {
                byBarcode.putIfAbsent(barcode, produto);
            }
        }

        List<ProdutoLookupItem> naoProntos = new ArrayList<>();
        for (EstoqueFisicoCsvService.EstoqueItem stock : base) {
            ProdutoEntity existente = this.findFirstMatch(stock, byLegacy, byBarcode);
            if (this.isReadyForSale(existente)) {
                continue;
            }
            naoProntos.add(this.buildPendingFromStock(stock, existente));
        }
        return naoProntos;
    }

    private ProdutoLookupItem buildPendingFromStock(EstoqueFisicoCsvService.EstoqueItem stock, ProdutoEntity existente) {
        if (existente != null) {
            return new ProdutoLookupItem(
                    existente.getId(),
                    existente.getLegacyId() != null ? existente.getLegacyId() : stock.legacyId(),
                    "CATALOGO_PENDENTE",
                    StringUtils.hasText(existente.getNome()) ? existente.getNome() : stock.nome(),
                    StringUtils.hasText(existente.getDescricao()) ? existente.getDescricao() : stock.nome(),
                    StringUtils.hasText(existente.getCategoria()) ? existente.getCategoria() : "Estoque fisico",
                    StringUtils.hasText(existente.getCodigoBarras()) ? existente.getCodigoBarras() : stock.codigoBarras(),
                    existente.getPrecoVenda(),
                    existente.getPrecoPromocional(),
                    existente.getEstoque() != null ? existente.getEstoque() : stock.estoque(),
                    StringUtils.hasText(existente.getFabricante()) ? existente.getFabricante() : stock.fabricante(),
                    existente.getUnidade()
            );
        }

        return new ProdutoLookupItem(
                null,
                stock.legacyId(),
                "ESTOQUE_FISICO",
                stock.nome(),
                stock.nome(),
                "Estoque fisico",
                stock.codigoBarras(),
                null,
                null,
                stock.estoque(),
                stock.fabricante(),
                ""
        );
    }

    private List<ProdutoLegacyEntity> buscarNoEstoqueFisico(
            ProdutoLegacyRepository legacyRepository,
            String termo,
            int limite
    ) {
        List<ProdutoLegacyEntity> itens = new ArrayList<>();
        String termoLimpo = this.normalize(termo);
        if (termoLimpo.isBlank()) {
            return itens;
        }

        String termoDigits = termoLimpo.replaceAll("\\D+", "");
        if (!termoDigits.isBlank() && termoDigits.length() >= 8) {
            legacyRepository.findByCodigoBarras(termoDigits).ifPresent(itens::add);
        }

        for (ProdutoLegacyEntity entity : legacyRepository.findByNomeContainingIgnoreCase(termoLimpo)) {
            if (entity == null || entity.getId() == null) {
                continue;
            }
            if (entity.getSaldo() != null && entity.getSaldo().signum() <= 0) {
                continue;
            }
            itens.add(entity);
            if (itens.size() >= Math.max(limite * 2, 25)) {
                break;
            }
        }

        LinkedHashMap<Integer, ProdutoLegacyEntity> dedupe = new LinkedHashMap<>();
        for (ProdutoLegacyEntity item : itens) {
            if (item != null && item.getId() != null) {
                dedupe.putIfAbsent(item.getId(), item);
            }
        }
        return dedupe.values().stream().limit(Math.max(limite, 1)).toList();
    }

    public static record ProdutoLookupItem(
            Long id,
            Long legacyId,
            String origem,
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
                    p.getLegacyId(),
                    "CATALOGO",
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

        static ProdutoLookupItem fromLegacy(ProdutoLegacyEntity p) {
            Integer estoqueLegacy = p.getSaldo() == null ? 0 : Math.max(0, p.getSaldo().intValue());
            String codigo = p.getCodigoBarras() == null ? "" : p.getCodigoBarras().replaceAll("\\D+", "");
            String nome = p.getNome() == null || p.getNome().isBlank() ? "Produto do estoque fisico" : p.getNome().trim();
            String descricao = p.getApresentacao() == null ? "" : p.getApresentacao().trim();
            String unidade = p.getApresentacao() == null ? "" : p.getApresentacao().trim();

            return new ProdutoLookupItem(
                    null,
                    p.getId() == null ? null : p.getId().longValue(),
                    "ESTOQUE_FISICO",
                    nome,
                    descricao,
                    "Estoque fisico",
                    codigo,
                    null,
                    null,
                    estoqueLegacy,
                    null,
                    unidade
            );
        }
    }

    public static record ProdutoLookupPage(
            List<ProdutoLookupItem> items,
            int page,
            int size,
            int total,
            boolean hasNext
    ) {
    }

    @Generated
    public ProdutoAdminPageController(ProdutoAdminService adminService,
                                      ProdutoRepository produtoRepository,
                                      ProdutoCategoriaRepository categoriaRepository,
                                      ImageStorageService imageStorageService,
                                      EstoqueFisicoCsvService estoqueFisicoCsvService,
                                      ObjectProvider<EstoqueFisicoImportService> estoqueImportServiceProvider,
                                      ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider,
                                      ObjectProvider<SincronizacaoCatalogoService> catalogSyncProvider) {
        this.adminService = adminService;
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.imageStorageService = imageStorageService;
        this.estoqueFisicoCsvService = estoqueFisicoCsvService;
        this.estoqueImportServiceProvider = estoqueImportServiceProvider;
        this.legacyRepositoryProvider = legacyRepositoryProvider;
        this.catalogSyncProvider = catalogSyncProvider;
    }
}



