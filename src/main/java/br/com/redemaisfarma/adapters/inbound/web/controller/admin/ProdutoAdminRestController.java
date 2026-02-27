package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.dto.request.AdminProdutoRequestDTO;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.domain.Produto;
import jakarta.validation.Valid;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/produtos")
public class ProdutoAdminRestController {

    private final ProdutoJpaRepository repo;
    private final ImageStorageService imageStorageService;

    @Generated
    public ProdutoAdminRestController(ProdutoJpaRepository repo, ImageStorageService imageStorageService) {
        this.repo = repo;
        this.imageStorageService = imageStorageService;
    }

    // ==================================================
    // 1. LISTAGEM PAGINADA
    // ==================================================
    @GetMapping
    public Page<ProdutoResponseDTO> list(
            @RequestParam(name = "q", required = false) String q,
            Pageable pageable) {
        return repo.searchPage(q, pageable).map(this::toResponse);
    }

    // ==================================================
    // 2. OBTÉM PRODUTO POR ID
    // ==================================================
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> get(@PathVariable("id") Long id) {
        return repo.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================================================
    // 3. CRIA NOVO PRODUTO
    // ==================================================
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> create(@Valid @RequestBody AdminProdutoRequestDTO dto) {
        ensureImageWhenActive(dto);
        ensureUniqueName(dto, null);
        ProdutoEntity entity = ProdutoMapper.toEntity(toDomain(dto));
        entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.API);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setDataImportacao(LocalDateTime.now());
        ProdutoEntity salvo = repo.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(salvo));
    }

    // ==================================================
    // 4. ATUALIZA PRODUTO EXISTENTE
    // ==================================================
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminProdutoRequestDTO dto) {

        ensureImageWhenActive(dto);
        ensureUniqueName(dto, id);
        return repo.findById(id)
                .map(atual -> {
                    Produto src = toDomain(dto);
                    ProdutoMapper.updateEntity(atual, src);
                    if (dto.getCodigoBarras() != null && !dto.getCodigoBarras().isBlank()) {
                        atual.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.API);
                    }
                    atual.setUpdatedAt(LocalDateTime.now());
                    ProdutoEntity salvo = repo.save(atual);
                    return ResponseEntity.ok(toResponse(salvo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================================================
    // 5. EXCLUI PRODUTO
    // ==================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================================================
    // 6. UPLOAD DE IMAGEM
    // ==================================================
    @PostMapping(value = "/{id}/imagem", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadImage(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {

        return repo.findById(id)
                .map(entity -> {
                    if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest().body("Arquivo vazio");
                    }

                    try {
                        String imageUrl = imageStorageService.saveProductImage(id, file);
                        entity.setImagem(imageUrl);
                        entity.setUpdatedAt(LocalDateTime.now());
                        repo.save(entity);
                        return ResponseEntity.ok(imageUrl);
                    } catch (IOException ex) {
                        return ResponseEntity.badRequest().body(ex.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================================================
    // 7. VALIDAR PRODUTO
    // ==================================================
    @PostMapping("/{id}/validar")
    public ResponseEntity<ProdutoResponseDTO> validar(
            @PathVariable("id") Long id,
            @RequestParam("validador") String validador) {

        return repo.findById(id)
                .map(entity -> {
                    if (entity.getStatus() == ProdutoStatus.PUBLICADO) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Produto publicado nao pode voltar para VALIDADO.");
                    }
                    entity.setStatus(ProdutoStatus.VALIDADO);
                    entity.setValidador(validador);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(toResponse(repo.save(entity)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================================================
    // 8. PUBLICAR PRODUTO
    // ==================================================
    @PostMapping("/{id}/publicar")
    public ResponseEntity<ProdutoResponseDTO> publicar(
            @PathVariable("id") Long id,
            @RequestParam("validador") String validador) {

        return repo.findById(id)
                .map(entity -> {
                    if (entity.getStatus() != ProdutoStatus.VALIDADO) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Produto deve estar VALIDADO antes de PUBLICAR.");
                    }
                    entity.setStatus(ProdutoStatus.PUBLICADO);
                    entity.setValidador(validador);
                    entity.setPublicadoEm(LocalDateTime.now());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(toResponse(repo.save(entity)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================================================
    // CONVERSORES AUXILIARES
    // ==================================================
    private Produto toDomain(AdminProdutoRequestDTO dto) {
        Produto d = new Produto();
        d.setId(null);
        d.setNome(dto.getNome());
        d.setDescricao(dto.getDescricao());
        d.setPrecoVenda(dto.getPreco());
        d.setImagem(dto.getImagem());
        d.setCategoria(dto.getCategoria());
        d.setCodigoBarras(dto.getCodigoBarras());
        d.setPrecoCusto(null);
        d.setEstoque(dto.getEstoque());
        d.setDisponivel(dto.getAtivo());
        d.setFabricante(null);
        d.setCodigoOriginal(null);
        d.setUnidade(null);
        d.setDataCadastro(LocalDateTime.now());
        return d;
    }

    private ProdutoResponseDTO toResponse(ProdutoEntity e) {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();

        UUID publicId = (e.getId() != null)
                ? UUID.nameUUIDFromBytes(("produto:" + e.getId()).getBytes())
                : UUID.randomUUID();

        dto.setEntityId(e.getId());
        dto.setId(publicId);
        dto.setNome(nvl(e.getNome(), "Produto"));
        dto.setDescricao(nvl(e.getDescricao(), ""));
        dto.setPreco(e.getPrecoVenda());
        dto.setImagem(e.getImagem());
        dto.setCategoria(e.getCategoria());
        dto.setEstoqueAtual(e.getEstoque());
        dto.setValidade(LocalDate.now().plusYears(1));
        dto.setCodigoBarras(e.getCodigoBarras());
        dto.setMarca(e.getFabricante());
        dto.setFornecedor(null);
        dto.setQuantidadeVendida(null);
        dto.setDataCadastro(e.getDataCadastro() != null ? e.getDataCadastro().atStartOfDay() : null);
        dto.setDataAtualizacao(e.getUpdatedAt());
        dto.setProdutoDestaque(Boolean.TRUE.equals(e.getDestaqueCarrossel()));
        dto.setProdutoRecomendadoIA(Boolean.FALSE);
        dto.setProdutoControlado(Boolean.FALSE);
        dto.setAvaliacaoMedia(null);
        dto.setTags(null);

        boolean ativo = Boolean.TRUE.equals(e.getDisponivel())
                && e.getPrecoVenda() != null
                && e.getPrecoVenda().signum() > 0
                && e.getEstoque() != null
                && e.getEstoque() > 0;

        dto.setSituacao(ativo
                ? ProdutoResponseDTO.SituacaoProduto.ATIVO
                : ProdutoResponseDTO.SituacaoProduto.ESGOTADO);

        return dto;
    }

    private void ensureImageWhenActive(AdminProdutoRequestDTO dto) {
        if (Boolean.TRUE.equals(dto.getAtivo())
                && (dto.getImagem() == null || dto.getImagem().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Imagem obrigatória para produtos disponibilizados na web.");
        }
    }

    private void ensureUniqueName(AdminProdutoRequestDTO dto, Long currentId) {
        if (dto.getNome() == null) {
            return;
        }
        String nome = dto.getNome().trim();
        if (nome.isBlank()) {
            return;
        }
        repo.findByNomeIgnoreCase(nome)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Produto com este nome já existe – pesquise antes de criar.");
                });
    }

    private static String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }
}
