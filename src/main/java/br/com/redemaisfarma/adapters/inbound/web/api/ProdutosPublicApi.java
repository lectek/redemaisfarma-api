/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.ArraySchema
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  jakarta.validation.constraints.Max
 *  jakarta.validation.constraints.Min
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.http.CacheControl
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.CrossOrigin
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.server.ResponseStatusException
 */
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
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Validated
@CrossOrigin
@RestController
@RequestMapping(value={"/api/public/produtos"})
@Tag(name="Produtos (P\u00fablico)", description="Consulta p\u00fablica de produtos da vitrine")
public class ProdutosPublicApi {
    private static final Set<String> SORT_WHITELIST = Set.of("nome", "preco", "dataAtualizacao");
    private final ProdutoJpaRepository repo;

    public ProdutosPublicApi(ProdutoJpaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @Operation(summary="Listar produtos (paginado)", description="Retorna uma p\u00e1gina de produtos. Suporta busca por `q` (nome/descri\u00e7\u00e3o/c\u00f3digo). Ordena\u00e7\u00e3o permitida: `nome|preco|dataAtualizacao`.", parameters={@Parameter(name="page", description="P\u00e1gina (0..N)", example="0"), @Parameter(name="size", description="Tamanho da p\u00e1gina (1..100)", example="24"), @Parameter(name="q", description="Termo de busca", example="whey"), @Parameter(name="sort", description="Campo para ordena\u00e7\u00e3o", example="nome"), @Parameter(name="dir", description="Dire\u00e7\u00e3o (asc|desc)", example="asc")}, responses={@ApiResponse(responseCode="200", description="OK", content={@Content(mediaType="application/json", schema=@Schema(implementation=Page.class))})})
    public ResponseEntity<Page<ProdutoResponseDTO>> listar(@RequestParam(defaultValue="0") @Min(value=0L) int page, @RequestParam(defaultValue="24") @Min(value=1L) @Max(value=100L) @Min(value=1L) @Max(value=100L) int size, @RequestParam(required=false, name="q") String q, @RequestParam(required=false, defaultValue="nome") String sort, @RequestParam(required=false, defaultValue="asc") String dir) {
        String term = q == null || q.isBlank() ? null : q.trim();
        Sort safeSort = this.buildSafeSort(sort, dir);
        PageRequest pageable = PageRequest.of((int)page, (int)size, (Sort)safeSort);
        Page<ProdutoEntity> dados = this.repo.searchPage(term, (Pageable)pageable);
        Page body = dados.map(ProdutoRestMapper::toResponse);
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().cacheControl(CacheControl.noCache())).body((Object)body);
    }

    @GetMapping(value={"/destaques"})
    @Operation(summary="Destaques do carrossel", description="Retorna uma lista curta de produtos destacados para a home.", parameters={@Parameter(name="limit", description="Quantidade (1..20)", example="10")}, responses={@ApiResponse(responseCode="200", description="OK", content={@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=ProdutoResponseDTO.class)))})})
    public ResponseEntity<List<ProdutoResponseDTO>> destaques(@RequestParam(defaultValue="10") @Min(value=1L) @Max(value=20L) @Min(value=1L) @Max(value=20L) int limit) {
        int safe = Math.min(Math.max(limit, 1), 20);
        PageRequest top = PageRequest.of((int)0, (int)safe);
        List<ProdutoEntity> first = this.repo.findCarrossel((Pageable)top);
        if (first == null || first.isEmpty()) {
            first = this.repo.findVitrineFallback((Pageable)top);
        }
        List<ProdutoResponseDTO> body = first.stream().map(ProdutoRestMapper::toResponse).toList();
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().cacheControl(CacheControl.maxAge((Duration)Duration.ofSeconds(30L)).cachePrivate())).body(body);
    }

    @GetMapping(value={"/{id}"})
    @Operation(summary="Obter produto por ID", description="Retorna os dados de um produto pelo seu ID interno (entityId).", parameters={@Parameter(name="id", description="ID interno do produto", example="48660")}, responses={@ApiResponse(responseCode="200", description="OK", content={@Content(mediaType="application/json", schema=@Schema(implementation=ProdutoResponseDTO.class))}), @ApiResponse(responseCode="404", description="Produto n\u00e3o encontrado")})
    public ResponseEntity<ProdutoResponseDTO> obter(@PathVariable(value="id") Long id) {
        ProdutoEntity p = (ProdutoEntity)this.repo.findById(id).orElseThrow(() -> new ResponseStatusException((HttpStatusCode)HttpStatus.NOT_FOUND, "Produto n\u00e3o encontrado"));
        return ResponseEntity.ok((Object)ProdutoRestMapper.toResponse(p));
    }

    private Sort buildSafeSort(String sort, String dir) {
        String candidate;
        String string = candidate = sort == null ? "nome" : sort.trim();
        if (!SORT_WHITELIST.contains(candidate)) {
            candidate = "nome";
        }
        boolean desc = "desc".equalsIgnoreCase(dir);
        Sort s = Sort.by((String[])new String[]{candidate});
        return desc ? s.descending() : s.ascending();
    }
}

