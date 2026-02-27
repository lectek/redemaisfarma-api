package br.com.redemaisfarma.adapters.inbound.web.api;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;
import br.com.redemaisfarma.application.mapper.ProdutoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Validated
@CrossOrigin
@RestController
@RequestMapping("/api/public/produtos")
@Tag(name = "Produtos (Público)", description = "Consulta pública de produtos da vitrine")
public class ProdutosPublicApi {

    private static final Set<String> SORT_WHITELIST = Set.of("nome", "preco", "dataAtualizacao");

    private final ProdutoJpaRepository repo;

    public ProdutosPublicApi(ProdutoJpaRepository repo) {
        this.repo = repo;
    }

    // ==================================================
    // 1. LISTAGEM PAGINADA
    // ==================================================
    @GetMapping
    @Operation(
        summary = "Listar produtos (paginado)",
        description = "Retorna uma página de produtos. Suporta busca por `q` (nome/descrição/código). " +
                      "Ordenação permitida: `nome|preco|dataAtualizacao`.",
        parameters = {
            @Parameter(name = "page", description = "Página (0..N)", example = "0"),
            @Parameter(name = "size", description = "Tamanho da página (1..100)", example = "24"),
            @Parameter(name = "q", description = "Termo de busca", example = "whey"),
            @Parameter(name = "sort", description = "Campo para ordenação", example = "nome"),
            @Parameter(name = "dir", description = "Direção (asc|desc)", example = "asc")
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "OK",
                content = @Content(mediaType = "application/json",
                                   schema = @Schema(implementation = Page.class))
            )
        }
    )
    public ResponseEntity<Page<ProdutoResponseDTO>> listar(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "24") @Min(1) @Max(100) int size,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        String term = (q == null || q.isBlank()) ? null : q.trim();
        Sort safeSort = buildSafeSort(sort, dir);
        Pageable pageable = PageRequest.of(page, size, safeSort);

        Page<ProdutoEntity> entities = repo.searchPublicPage(term, pageable);
        Page<ProdutoResponseDTO> body = entities.map(ProdutoRestMapper::toResponse);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)).cachePrivate())
                .body(body);
    }

    // ==================================================
    // 2. LISTA DE DESTAQUES
    // ==================================================
    @GetMapping("/destaques")
    @Operation(
        summary = "Destaques do carrossel",
        description = "Retorna uma lista curta de produtos destacados para a home.",
        parameters = {
            @Parameter(name = "limit", description = "Quantidade (1..20)", example = "10")
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "OK",
                content = @Content(mediaType = "application/json",
                                   array = @ArraySchema(schema = @Schema(implementation = ProdutoResponseDTO.class)))
            )
        }
    )
    public ResponseEntity<List<ProdutoResponseDTO>> destaques(
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit) {

        int safe = Math.min(Math.max(limit, 1), 20);
        Pageable top = PageRequest.of(0, safe);

        List<ProdutoEntity> produtos = repo.findCarrossel(top);
        if (produtos == null || produtos.isEmpty()) {
            produtos = repo.findVitrineFallback(top);
        }

        List<ProdutoResponseDTO> body = produtos.stream()
                .map(ProdutoRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)).cachePrivate())
                .body(body);
    }

    // ==================================================
    // 3. OBTÉM PRODUTO POR ID
    // ==================================================
    @GetMapping("/{id}")
    @Operation(
        summary = "Obter produto por ID",
        description = "Retorna os dados de um produto pelo seu ID interno (entityId).",
        parameters = {
            @Parameter(name = "id", description = "ID interno do produto", example = "48660")
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "OK",
                content = @Content(mediaType = "application/json",
                                   schema = @Schema(implementation = ProdutoResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        }
    )
    public ResponseEntity<ProdutoResponseDTO> obter(@PathVariable("id") Long id) {
        ProdutoEntity produto = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        return ResponseEntity.ok(ProdutoRestMapper.toResponse(produto));
    }

    // ==================================================
    // 4. SORT SEGURO
    // ==================================================
    private Sort buildSafeSort(String sort, String dir) {
        String candidate = (sort == null || sort.isBlank()) ? "nome" : sort.trim();
        if (!SORT_WHITELIST.contains(candidate)) {
            candidate = "nome";
        }
        String entityField = switch (candidate) {
            case "preco" -> "precoVenda";
            case "dataAtualizacao" -> "updatedAt";
            default -> "nome";
        };
        Sort s = Sort.by(entityField);
        return "desc".equalsIgnoreCase(dir) ? s.descending() : s.ascending();
    }
}
