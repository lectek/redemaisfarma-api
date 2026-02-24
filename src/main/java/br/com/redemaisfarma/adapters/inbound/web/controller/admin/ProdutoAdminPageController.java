package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.service.ProdutoAdminService;
import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
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
    private final ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider;
    private final ObjectProvider<SincronizacaoCatalogoService> catalogSyncProvider;

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
        model.addAttribute("legacySyncEnabled", this.catalogSyncProvider.getIfAvailable() != null);
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

        boolean publicavel = Boolean.TRUE.equals(salvo.getDisponivel())
                && this.isPositivePrice(salvo.getPrecoVenda())
                && (salvo.getEstoque() != null && salvo.getEstoque() > 0)
                && StringUtils.hasText(salvo.getImagem());

        if (publicavel) {
            salvo.setStatus(ProdutoStatus.PUBLICADO);
            if (salvo.getPublicadoEm() == null) {
                salvo.setPublicadoEm(LocalDateTime.now());
            }
        } else {
            salvo.setStatus(ProdutoStatus.IMPORTADO);
            salvo.setPublicadoEm(null);
        }
        produtoRepository.save(salvo);

        ra.addFlashAttribute("success", "Produto criado com sucesso.");
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
            @RequestParam(defaultValue = "8") int limit
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

    private String normalizeBarcode(String value) {
        return this.normalize(value).replaceAll("\\D+", "");
    }

    private boolean isPositivePrice(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
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

    @Generated
    public ProdutoAdminPageController(ProdutoAdminService adminService,
                                      ProdutoRepository produtoRepository,
                                      ProdutoCategoriaRepository categoriaRepository,
                                      ImageStorageService imageStorageService,
                                      ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider,
                                      ObjectProvider<SincronizacaoCatalogoService> catalogSyncProvider) {
        this.adminService = adminService;
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.imageStorageService = imageStorageService;
        this.legacyRepositoryProvider = legacyRepositoryProvider;
        this.catalogSyncProvider = catalogSyncProvider;
    }
}



