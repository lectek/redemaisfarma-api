package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.TarjaMedicacao;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
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
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/produtos")
public class ProdutoAdminPageController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoAdminPageController.class);
    private static final String CATEGORIA_ESTOQUE_FISICO = "Estoque fisico";
    private static final String CATEGORIA_MEDICACOES = "Medicacoes";
    private static final String MODEL_ATTR_CATEGORIAS = "categorias";
    private static final String FLASH_SUCCESS = "success";
    private static final String FLASH_WARNING = "warning";
    private static final String PATH_PRODUTOS = "/admin/produtos";
    private static final String REDIRECT_PRODUTOS = "redirect:" + PATH_PRODUTOS;
    private static final String REDIRECT_PRODUTOS_BASE = "redirect:/admin/produtos/";
    private static final String REDIRECT_PRODUTOS_NOVO = "redirect:/admin/produtos/novo";
    private static final String SUFFIX_EDITAR = "/editar";
    private static final int MAX_PENDING_ALL_LIMIT = 200_000;
    private static final String KEY_ALERTA_ESTOQUE_ENABLED = "app.estoque.alerta.enabled";
    private static final String KEY_ALERTA_ESTOQUE_LIMITE = "app.estoque.alerta.limite";
    private static final int DEFAULT_ALERTA_ESTOQUE_LIMITE = 10;

    private final ProdutoAdminService adminService;
    private final ProdutoRepository produtoRepository;
    private final ProdutoCategoriaRepository categoriaRepository;
    private final ImageStorageService imageStorageService;
    private final AppSettingService appSettingService;
    private final ObjectProvider<EstoqueFisicoCsvService> estoqueFisicoCsvServiceProvider;
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
        model.addAttribute(MODEL_ATTR_CATEGORIAS, this.resolveCategorias());
        model.addAttribute("legacySyncEnabled", this.catalogSyncProvider.getIfAvailable() != null);
        this.populateEstoqueAlerta(model);
        return "pages/admin/produtos/lista";
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "limit", defaultValue = "2000") int limit
    ) {
        int safeLimit = Math.clamp(limit, 1, 50_000);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String filename = "produtos-" + ts + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        // lambda sem casts para preservar o tipo genérico
        StreamingResponseBody body = os -> {
            try {
                adminService.writeCsv(os, q, categoria, safeLimit);
            } catch (IOException e) { // se writeCsv lancar checked
                throw new UncheckedIOException("Falha ao gerar CSV", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    @PostMapping(consumes = "multipart/form-data")
    @SuppressWarnings("java:S3776")
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
        this.applyMedicacaoRules(produto);

        if (produto.getLegacyId() != null && produto.getLegacyId() <= 0) {
            produto.setLegacyId(null);
        }

        if (produto.getLegacyId() != null) {
            Optional<ProdutoEntity> existente = produtoRepository.findByLegacyId(produto.getLegacyId());
            if (existente.isPresent()) {
                ra.addFlashAttribute("info", "Produto ja existe no catalogo. Abrindo edicao.");
                return REDIRECT_PRODUTOS_BASE + existente.get().getId() + SUFFIX_EDITAR;
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
                return REDIRECT_PRODUTOS_BASE + existentePorCodigo.get().getId() + SUFFIX_EDITAR;
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

        ra.addFlashAttribute(FLASH_SUCCESS, "Produto criado em IMPORTADO. Valide e publique na edicao.");
        return REDIRECT_PRODUTOS_BASE + salvo.getId() + SUFFIX_EDITAR;
    }

    @GetMapping("/novo")
    public String novoProdutoPage(Model model) {
        model.addAttribute("produto", new ProdutoEntity());
        model.addAttribute(MODEL_ATTR_CATEGORIAS, this.resolveCategorias());
        model.addAttribute("legacySyncEnabled", this.catalogSyncProvider.getIfAvailable() != null);
        this.populateEstoqueAlerta(model);
        return "pages/admin/produtos/form";
    }

    @PostMapping("/alerta-estoque")
    public String atualizarAlertaEstoque(@RequestParam(name = "limite", required = false) Integer limite,
                                         @RequestParam(name = "redirect", required = false) String redirect,
                                         RedirectAttributes ra) {
        int safeLimit = limite == null
                ? DEFAULT_ALERTA_ESTOQUE_LIMITE
                : Math.clamp(limite, 1, 100_000);
        this.appSettingService.upsert(
                KEY_ALERTA_ESTOQUE_LIMITE,
                String.valueOf(safeLimit),
                "Limite de estoque baixo para aviso ao admin"
        );
        ra.addFlashAttribute(FLASH_SUCCESS, "Limite de alerta de estoque atualizado para " + safeLimit + ".");
        return "redirect:" + this.resolveRedirectPath(redirect);
    }

    @GetMapping(value = "/busca-rapida", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @SuppressWarnings({"java:S3776", "java:S135"})
    public List<ProdutoLookupItem> buscaRapida(
            @RequestParam("q") String q,
            @RequestParam(name = "limit", defaultValue = "8") int limit
    ) {
        String termo = this.normalize(q);
        if (termo.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.clamp(limit, 1, 20);
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
        String termo = this.normalizeQuery(q);
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, 40);
        NaoProntosSlice slice = this.fetchNaoProntosSlice(termo, safePage, safeSize);
        return new ProdutoLookupPage(slice.items(), safePage, safeSize, slice.total(), slice.hasNext());
    }

    @GetMapping("/nao-prontos/todos")
    public String listarNaoProntosTodos(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size,
            Model model
    ) {
        CatalogoSlice slice = this.fetchCatalogoSlice(q, page, size);
        model.addAttribute("pendingItems", slice.items());
        model.addAttribute("pendingTotal", slice.total());
        model.addAttribute("listedCount", slice.items().size());
        model.addAttribute("pageNumber", slice.page());
        model.addAttribute("pageSize", slice.size());
        model.addAttribute("totalPages", slice.totalPages());
        model.addAttribute("hasPrev", slice.hasPrev());
        model.addAttribute("hasNext", slice.hasNext());
        model.addAttribute("q", q == null ? "" : q.trim());
        return "pages/admin/produtos/nao-prontos-todos";
    }

    @PostMapping("/nao-prontos/publicar-aptos")
    @SuppressWarnings("java:S3776")
    public String publicarNaoProntosAptosEmLote(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "validador", required = false) String validador,
            RedirectAttributes ra
    ) {
        List<ProdutoLookupItem> pendentes = this.resolveNaoProntosFromDatabase(q, MAX_PENDING_ALL_LIMIT, false);
        if (pendentes.isEmpty()) {
            ra.addFlashAttribute("info", "Nenhum produto pendente encontrado para o filtro informado.");
            return this.redirectNaoProntosTodos(q);
        }

        List<Long> ids = pendentes.stream()
                .map(ProdutoLookupItem::id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            ra.addFlashAttribute(FLASH_WARNING, "Nenhum item do filtro possui cadastro no catalogo para publicacao.");
            return this.redirectNaoProntosTodos(q);
        }

        List<ProdutoEntity> entidades = this.produtoRepository.findAllById(ids);
        if (entidades.isEmpty()) {
            ra.addFlashAttribute(FLASH_WARNING, "Nao foi possivel localizar os produtos pendentes no catalogo.");
            return this.redirectNaoProntosTodos(q);
        }

        String responsavel = StringUtils.hasText(validador) ? validador.trim() : "admin-lote";
        LocalDateTime now = LocalDateTime.now();

        int bloqueados = 0;
        int semImagem = 0;
        int semPreco = 0;
        int semEstoque = 0;
        List<ProdutoEntity> paraPublicar = new ArrayList<>();

        for (ProdutoEntity entity : entidades) {
            boolean pronto = true;

            if (!this.temImagem(entity)) {
                semImagem++;
                pronto = false;
            }
            if (!this.temPrecoPositivo(entity)) {
                semPreco++;
                pronto = false;
            }
            if (!this.temEstoquePositivo(entity)) {
                semEstoque++;
                pronto = false;
            }

            if (!pronto) {
                bloqueados++;
                continue;
            }

            entity.setDisponivel(Boolean.TRUE);
            entity.setStatus(ProdutoStatus.PUBLICADO);
            entity.setValidador(responsavel);
            entity.setPublicadoEm(now);
            entity.setDespublicadoEm(null);
            entity.setUpdatedAt(now);
            paraPublicar.add(entity);
        }

        if (!paraPublicar.isEmpty()) {
            this.produtoRepository.saveAll(paraPublicar);
            ra.addFlashAttribute(FLASH_SUCCESS,
                    "Publicacao em lote concluida: " + paraPublicar.size() + " produto(s) publicado(s).");
        } else {
            ra.addFlashAttribute(FLASH_WARNING,
                    "Nenhum produto apto para publicar. Corrija imagem, preco e estoque dos pendentes.");
        }

        if (bloqueados > 0) {
            ra.addFlashAttribute("info",
                    "Bloqueios encontrados em " + bloqueados + " item(ns): sem imagem " + semImagem
                            + ", sem preco " + semPreco + ", sem estoque " + semEstoque + ".");
        }

        return this.redirectNaoProntosTodos(q);
    }

    @PostMapping("/sincronizar-estoque")
    @SuppressWarnings("java:S3516")
    public String sincronizarEstoqueFisico(RedirectAttributes ra) {
        SincronizacaoCatalogoService syncService = this.catalogSyncProvider.getIfAvailable();
        if (syncService == null) {
            ra.addFlashAttribute(FLASH_WARNING, "Sincronizacao indisponivel. Ative legacy.sync.enabled e configure Firebird.");
            return REDIRECT_PRODUTOS;
        }

        try {
            SincronizacaoCatalogoService.ResumoSync resumo = syncService.sincronizarTudo();
            ra.addFlashAttribute(FLASH_SUCCESS,
                    "Sincronizacao concluida. Lidos: " + resumo.lidos()
                            + ", inseridos: " + resumo.inseridos()
                            + ", atualizados: " + resumo.atualizados()
                            + ", ignorados: " + resumo.ignorados()
                            + ", erros: " + resumo.erros() + ".");
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao sincronizar estoque fisico", ex);
            ra.addFlashAttribute("error", "Falha ao sincronizar estoque fisico. Verifique as configuracoes do legado.");
        }
        return REDIRECT_PRODUTOS;
    }

    @PostMapping("/importar-estoque-fisico")
    @SuppressWarnings("java:S3516")
    public String importarEstoqueFisico(RedirectAttributes ra) {
        EstoqueFisicoImportService importService = this.estoqueImportServiceProvider.getIfAvailable();
        if (importService == null) {
            ra.addFlashAttribute(FLASH_WARNING, "Importacao CSV indisponivel no ambiente atual.");
            return REDIRECT_PRODUTOS_NOVO;
        }

        try {
            EstoqueFisicoImportService.ImportacaoResumo resumo = importService.importarTodosComoNaoDisponiveis();
            ra.addFlashAttribute(FLASH_SUCCESS,
                    "Importacao concluida: lidos " + resumo.lidos()
                            + ", inseridos " + resumo.inseridos()
                            + ", atualizados " + resumo.atualizados()
                            + ", ignorados " + resumo.ignorados()
                            + ", erros " + resumo.erros() + ".");
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao importar estoque fisico CSV", ex);
            ra.addFlashAttribute("error", "Falha ao importar estoque fisico. Verifique o arquivo CSV.");
        }
        return REDIRECT_PRODUTOS_NOVO;
    }

    @GetMapping("/form")
    public String redirectFormToNovo() {
        return REDIRECT_PRODUTOS_NOVO;
    }

    @GetMapping("/{id}/editar")
    public String editarProdutoPage(@PathVariable("id") Long id, Model model) {
        model.addAttribute("produtoId", id);
        this.produtoRepository.findById(id).ifPresent(produto -> model.addAttribute("produto", produto));
        model.addAttribute(MODEL_ATTR_CATEGORIAS, this.resolveCategorias());
        this.populateEstoqueAlerta(model);
        return "pages/admin/produtos/editar";
    }

    private List<String> resolveCategorias() {
        List<String> categorias = this.categoriaRepository.findAllNomes();
        if (categorias == null || categorias.isEmpty()) {
            return List.of("Sem Categoria", CATEGORIA_MEDICACOES);
        }
        if (categorias.stream().noneMatch(this::isCategoriaMedicacoes)) {
            List<String> enriched = new ArrayList<>(categorias);
            enriched.add(CATEGORIA_MEDICACOES);
            return enriched;
        }
        return categorias;
    }

    private void applyMedicacaoRules(ProdutoEntity produto) {
        boolean categoriaMedicacoes = isCategoriaMedicacoes(produto.getCategoria());
        if (!categoriaMedicacoes) {
            produto.setTarjaMedicacao(null);
            produto.setExigeReceita(Boolean.FALSE);
            return;
        }

        TarjaMedicacao tarja = produto.getTarjaMedicacao();
        if (tarja == null) {
            tarja = TarjaMedicacao.SEM_TARJA;
            produto.setTarjaMedicacao(tarja);
        }

        boolean exigeReceita = switch (tarja) {
            case SEM_TARJA -> false;
            case TARJA_AMARELA -> Boolean.TRUE.equals(produto.getExigeReceita());
            case TARJA_VERMELHA, TARJA_PRETA -> true;
        };
        produto.setExigeReceita(exigeReceita);
    }

    private boolean isCategoriaMedicacoes(String categoria) {
        return CATEGORIA_MEDICACOES.equalsIgnoreCase(this.normalizeCategoria(categoria));
    }

    private String normalizeCategoria(String categoria) {
        String safe = this.normalize(categoria);
        if (safe.isEmpty()) {
            return safe;
        }
        return Normalizer.normalize(safe, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeBarcode(String value) {
        return BarcodeNormalizer.normalize(value);
    }

    private String normalizeQuery(String value) {
        String termo = this.normalize(value);
        return termo.isBlank() ? null : termo;
    }

    private void populateEstoqueAlerta(Model model) {
        boolean alertaAtivo = this.appSettingService.getBoolean(KEY_ALERTA_ESTOQUE_ENABLED, true);
        int limite = Math.max(1, this.appSettingService.getInt(KEY_ALERTA_ESTOQUE_LIMITE, DEFAULT_ALERTA_ESTOQUE_LIMITE));
        int qtdBaixo = this.produtoRepository.findComEstoqueBaixo(limite).size();

        model.addAttribute("alertaEstoqueAtivo", alertaAtivo);
        model.addAttribute("alertaEstoqueLimite", limite);
        model.addAttribute("alertaEstoqueBaixoTotal", qtdBaixo);
    }

    private String resolveRedirectPath(String redirect) {
        String path = this.normalize(redirect);
        if (!StringUtils.hasText(path)) {
            return PATH_PRODUTOS;
        }
        if (!path.startsWith(PATH_PRODUTOS)) {
            return PATH_PRODUTOS;
        }
        return path;
    }

    private String redirectNaoProntosTodos(String q) {
        String termo = this.normalize(q);
        if (termo.isBlank()) {
            return "redirect:/admin/produtos/nao-prontos/todos";
        }
        String encoded = URLEncoder.encode(termo, StandardCharsets.UTF_8);
        return "redirect:/admin/produtos/nao-prontos/todos?q=" + encoded;
    }

    private boolean temImagem(ProdutoEntity entity) {
        return entity != null && StringUtils.hasText(entity.getImagem());
    }

    private boolean temPrecoPositivo(ProdutoEntity entity) {
        return entity != null
                && entity.getPrecoVenda() != null
                && entity.getPrecoVenda().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean temEstoquePositivo(ProdutoEntity entity) {
        return entity != null
                && entity.getEstoque() != null
                && entity.getEstoque() > 0;
    }

    private List<ProdutoLookupItem> resolveNaoProntosFromDatabase(String q, int limit) {
        return this.resolveNaoProntosFromDatabase(q, limit, true);
    }

    @SuppressWarnings({"java:S3776", "java:S135"})
    private List<ProdutoLookupItem> resolveNaoProntosFromDatabase(String q, int limit, boolean fallbackToAllWhenEmpty) {
        String termo = this.normalizeQuery(q);
        int safeLimit = Math.clamp(limit, 1, MAX_PENDING_ALL_LIMIT);
        List<ProdutoLookupItem> itens = new ArrayList<>(Math.min(safeLimit, 2_000));
        Set<String> seen = new HashSet<>();
        int page = 0;

        while (itens.size() < safeLimit) {
            int pageSize = Math.min(1000, safeLimit - itens.size());
            Page<ProdutoEntity> result = this.fetchNaoProntosPage(termo, page, pageSize);
            if (result.isEmpty()) {
                break;
            }

            int addedOnPage = 0;
            for (ProdutoEntity entity : result.getContent()) {
                if (entity == null) {
                    continue;
                }
                if (itens.size() >= safeLimit) {
                    break;
                }
                ProdutoLookupItem mapped = this.buildPendingFromDatabase(entity);
                String key = this.pendingUniqueKey(mapped);
                if (!seen.add(key)) {
                    continue;
                }
                itens.add(mapped);
                addedOnPage++;
            }

            if (!result.hasNext()) {
                break;
            }
            if (addedOnPage == 0) {
                break;
            }
            page++;
        }

        List<ProdutoLookupItem> merged = this.mergeNaoProntosWithCsv(termo, itens, safeLimit);
        if (fallbackToAllWhenEmpty && merged.isEmpty() && StringUtils.hasText(termo)) {
            return this.resolveNaoProntosFromDatabase(null, safeLimit, false);
        }
        return merged;
    }

    private NaoProntosSlice fetchNaoProntosSlice(String termo, int page, int size) {
        List<ProdutoLookupItem> allItems = this.resolveNaoProntosFromDatabase(termo, MAX_PENDING_ALL_LIMIT);
        if (allItems.isEmpty()) {
            return new NaoProntosSlice(List.of(), 0, false);
        }

        int from = page * size;
        if (from >= allItems.size()) {
            return new NaoProntosSlice(List.of(), allItems.size(), false);
        }
        int to = Math.min(from + size, allItems.size());
        List<ProdutoLookupItem> content = allItems.subList(from, to);
        return new NaoProntosSlice(content, allItems.size(), to < allItems.size());
    }

    private CatalogoSlice fetchCatalogoSlice(String q, int page, int size) {
        String termo = this.normalizeQuery(q);
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 10, 200);
        Page<ProdutoEntity> result = this.fetchCatalogoPage(termo, safePage, safeSize);

        // Mantem a listagem util para o admin mesmo quando o filtro nao retorna itens.
        // Nesse caso exibe a primeira pagina do catalogo completo.
        if (StringUtils.hasText(termo) && result.getTotalElements() == 0L) {
            result = this.fetchCatalogoPage(null, 0, safeSize);
        }

        List<ProdutoLookupItem> items = result.getContent()
                .stream()
                .map(ProdutoLookupItem::from)
                .toList();

        return new CatalogoSlice(
                items,
                result.getTotalElements(),
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.hasPrevious(),
                result.hasNext()
        );
    }

    private Page<ProdutoEntity> fetchCatalogoPage(String termo, int page, int size) {
        PageRequest req = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return this.produtoRepository.searchPageByCategoria(termo, null, req);
    }

    private Page<ProdutoEntity> fetchNaoProntosPage(String termo, int page, int size) {
        PageRequest req = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<ProdutoEntity> byCategoria = this.produtoRepository.searchNaoDisponiveisByCategoria(
                termo,
                CATEGORIA_ESTOQUE_FISICO,
                req
        );
        if (byCategoria.hasContent()) {
            return byCategoria;
        }
        return this.produtoRepository.searchNaoDisponiveis(termo, req);
    }

    private ProdutoLookupItem buildPendingFromDatabase(ProdutoEntity produto) {
        return new ProdutoLookupItem(
                produto.getId(),
                produto.getLegacyId(),
                "CATALOGO_PENDENTE",
                produto.getNome(),
                StringUtils.hasText(produto.getDescricao()) ? produto.getDescricao() : produto.getNome(),
                StringUtils.hasText(produto.getCategoria()) ? produto.getCategoria() : CATEGORIA_ESTOQUE_FISICO,
                produto.getCodigoBarras(),
                produto.getPrecoVenda(),
                produto.getPrecoPromocional(),
                produto.getEstoque(),
                produto.getFabricante(),
                produto.getUnidade()
        );
    }

    @SuppressWarnings({"java:S3776", "java:S135"})
    private List<ProdutoLookupItem> mergeNaoProntosWithCsv(String termo,
                                                           List<ProdutoLookupItem> bancoItens,
                                                           int limit) {
        int safeLimit = Math.max(1, limit);
        if (bancoItens.size() >= safeLimit) {
            return bancoItens;
        }

        EstoqueFisicoCsvService csvService = this.estoqueFisicoCsvServiceProvider.getIfAvailable();
        if (csvService == null) {
            return bancoItens;
        }

        List<EstoqueFisicoCsvService.EstoqueItem> csvItems = csvService.search(termo == null ? "" : termo);
        if (csvItems.isEmpty()) {
            return bancoItens;
        }

        List<ProdutoLookupItem> merged = new ArrayList<>(Math.min(safeLimit, bancoItens.size() + csvItems.size()));
        merged.addAll(bancoItens);

        Set<Long> legacyIds = new HashSet<>();
        Set<String> barcodes = new HashSet<>();
        for (ProdutoLookupItem item : bancoItens) {
            if (item == null) {
                continue;
            }
            if (item.legacyId() != null) {
                legacyIds.add(item.legacyId());
            }
            String normalizedBarcode = this.normalizeBarcode(item.codigoBarras());
            if (StringUtils.hasText(normalizedBarcode)) {
                barcodes.add(normalizedBarcode);
            }
        }

        for (EstoqueFisicoCsvService.EstoqueItem csvItem : csvItems) {
            if (merged.size() >= safeLimit || csvItem == null) {
                break;
            }

            Long csvLegacyId = csvItem.legacyId();
            String csvBarcode = this.normalizeBarcode(csvItem.codigoBarras());
            boolean duplicateByLegacy = csvLegacyId != null && legacyIds.contains(csvLegacyId);
            boolean duplicateByBarcode = StringUtils.hasText(csvBarcode) && barcodes.contains(csvBarcode);
            if (duplicateByLegacy || duplicateByBarcode) {
                continue;
            }

            merged.add(this.buildPendingFromCsv(csvItem));
            if (csvLegacyId != null) {
                legacyIds.add(csvLegacyId);
            }
            if (StringUtils.hasText(csvBarcode)) {
                barcodes.add(csvBarcode);
            }
        }

        return merged;
    }

    private ProdutoLookupItem buildPendingFromCsv(EstoqueFisicoCsvService.EstoqueItem csvItem) {
        String nome = StringUtils.hasText(csvItem.nome()) ? csvItem.nome() : "Produto do estoque fisico";
        Integer estoque = csvItem.estoque() == null ? 0 : Math.max(0, csvItem.estoque());

        return new ProdutoLookupItem(
                null,
                csvItem.legacyId(),
                "ESTOQUE_FISICO",
                nome,
                nome,
                CATEGORIA_ESTOQUE_FISICO,
                csvItem.codigoBarras(),
                csvItem.precoVenda(),
                null,
                estoque,
                csvItem.fabricante(),
                null
        );
    }

    private String pendingUniqueKey(ProdutoLookupItem item) {
        if (item.id() != null) {
            return "ID:" + item.id();
        }
        if (item.legacyId() != null) {
            return "L:" + item.legacyId();
        }
        String normalizedBarcode = this.normalizeBarcode(item.codigoBarras());
        if (StringUtils.hasText(normalizedBarcode)) {
            return "B:" + normalizedBarcode;
        }
        return "N:" + this.normalize(item.nome()).toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings({"java:S3776", "java:S135"})
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
                    CATEGORIA_ESTOQUE_FISICO,
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

    private record NaoProntosSlice(
            List<ProdutoLookupItem> items,
            int total,
            boolean hasNext
    ) {
    }

    private record CatalogoSlice(
            List<ProdutoLookupItem> items,
            long total,
            int page,
            int size,
            int totalPages,
            boolean hasPrev,
            boolean hasNext
    ) {
    }

    @Generated
    @SuppressWarnings("java:S107")
    public ProdutoAdminPageController(ProdutoAdminService adminService,
                                      ProdutoRepository produtoRepository,
                                      ProdutoCategoriaRepository categoriaRepository,
                                      ImageStorageService imageStorageService,
                                      AppSettingService appSettingService,
                                      ObjectProvider<EstoqueFisicoCsvService> estoqueFisicoCsvServiceProvider,
                                      ObjectProvider<EstoqueFisicoImportService> estoqueImportServiceProvider,
                                      ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider,
                                      ObjectProvider<SincronizacaoCatalogoService> catalogSyncProvider) {
        this.adminService = adminService;
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.imageStorageService = imageStorageService;
        this.appSettingService = appSettingService;
        this.estoqueFisicoCsvServiceProvider = estoqueFisicoCsvServiceProvider;
        this.estoqueImportServiceProvider = estoqueImportServiceProvider;
        this.legacyRepositoryProvider = legacyRepositoryProvider;
        this.catalogSyncProvider = catalogSyncProvider;
    }
}
