package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.dto.request.AdminProdutoRequestDTO;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.domain.Produto;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/produtos")
public final class ProdutoAdminRestController {

    /**
     * Product prefix used when deriving public UUID from entity id.
     */
    private static final String PUBLIC_ID_PREFIX = "produto:";

    /**
     * Number of years used for synthetic validity in response.
     */
    private static final int DEFAULT_VALIDADE_YEARS = 1;

    /**
     * Conflict message when trying to validate an already published product.
     */
    private static final String VALIDAR_STATUS_CONFLICT =
            "Produto publicado nao pode voltar para VALIDADO.";

    /**
     * Conflict message when trying to publish a non-validated product.
     */
    private static final String PUBLICAR_STATUS_CONFLICT =
            "Produto deve estar VALIDADO antes de PUBLICAR.";

    /**
     * Validation message for duplicate names.
     */
    private static final String DUPLICATE_NAME_MESSAGE =
            "Produto com este nome já existe – pesquise antes de criar.";

    /**
     * Repository used for product persistence.
     */
    private final ProdutoJpaRepository repo;

    /**
     * Service used for image upload.
     */
    private final ImageStorageService imageStorageService;

    /**
     * Creates controller with dependencies.
     *
     * @param repository product repository
     * @param storageService image storage service
     */
    public ProdutoAdminRestController(
            final ProdutoJpaRepository repository,
            final ImageStorageService storageService
    ) {
        this.repo = repository;
        this.imageStorageService = storageService;
    }

    /**
     * Lists products using optional query and pagination.
     *
     * @param q optional text query
     * @param pageable page request
     * @return page of product responses
     */
    @GetMapping
    public Page<ProdutoResponseDTO> list(
            @RequestParam(name = "q", required = false) final String q,
            final Pageable pageable
    ) {
        return repo.searchPage(q, pageable).map(this::toResponse);
    }

    /**
     * Gets one product by id.
     *
     * @param id product id
     * @return product payload or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> get(
            @PathVariable("id") final Long id
    ) {
        return repo.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new product.
     *
     * @param dto request payload
     * @return created product response
     */
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> create(
            @Valid @RequestBody final AdminProdutoRequestDTO dto
    ) {
        ensureImageWhenActive(dto);
        ensureUniqueName(dto, null);

        final ProdutoEntity entity = ProdutoMapper.toEntity(toDomain(dto));
        entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.API);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setDataImportacao(LocalDateTime.now());

        final ProdutoEntity salvo = repo.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(salvo));
    }

    /**
     * Updates one product.
     *
     * @param id product id
     * @param dto request payload
     * @return updated product response or 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> update(
            @PathVariable("id") final Long id,
            @Valid @RequestBody final AdminProdutoRequestDTO dto
    ) {
        ensureImageWhenActive(dto);
        ensureUniqueName(dto, id);

        return repo.findById(id)
                .map(atual -> {
                    final Produto src = toDomain(dto);
                    ProdutoMapper.updateEntity(atual, src);
                    if (dto.getCodigoBarras() != null
                            && !dto.getCodigoBarras().isBlank()) {
                        atual.setMetodoLeituraCodigoBarras(
                                MetodoLeituraCodigoBarras.API
                        );
                    }
                    atual.setUpdatedAt(LocalDateTime.now());
                    final ProdutoEntity salvo = repo.save(atual);
                    return ResponseEntity.ok(toResponse(salvo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes one product.
     *
     * @param id product id
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads image for one product.
     *
     * @param id product id
     * @param file image file
     * @return image URL or error
     */
    @PostMapping(value = "/{id}/imagem", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadImage(
            @PathVariable("id") final Long id,
            @RequestParam("file") final MultipartFile file
    ) {
        return repo.findById(id)
                .map(entity -> {
                    if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("Arquivo vazio");
                    }
                    try {
                        final String imageUrl = imageStorageService
                                .saveProductImage(id, file);
                        entity.setImagem(imageUrl);
                        entity.setUpdatedAt(LocalDateTime.now());
                        repo.save(entity);
                        return ResponseEntity.ok(imageUrl);
                    } catch (IOException ex) {
                        return ResponseEntity.badRequest()
                                .body(ex.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Marks product as validated.
     *
     * @param id product id
     * @param validador validator name
     * @return updated product response or 404
     */
    @PostMapping("/{id}/validar")
    public ResponseEntity<ProdutoResponseDTO> validar(
            @PathVariable("id") final Long id,
            @RequestParam("validador") final String validador
    ) {
        return repo.findById(id)
                .map(entity -> {
                    if (entity.getStatus() == ProdutoStatus.PUBLICADO) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                VALIDAR_STATUS_CONFLICT
                        );
                    }
                    entity.setStatus(ProdutoStatus.VALIDADO);
                    entity.setValidador(validador);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(toResponse(repo.save(entity)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Publishes product when it is already validated.
     *
     * @param id product id
     * @param validador validator name
     * @return updated product response or 404
     */
    @PostMapping("/{id}/publicar")
    public ResponseEntity<ProdutoResponseDTO> publicar(
            @PathVariable("id") final Long id,
            @RequestParam("validador") final String validador
    ) {
        return repo.findById(id)
                .map(entity -> {
                    if (entity.getStatus() != ProdutoStatus.VALIDADO) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                PUBLICAR_STATUS_CONFLICT
                        );
                    }
                    entity.setStatus(ProdutoStatus.PUBLICADO);
                    entity.setValidador(validador);
                    entity.setPublicadoEm(LocalDateTime.now());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(toResponse(repo.save(entity)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Converts request DTO to domain object.
     *
     * @param dto request payload
     * @return domain object
     */
    private Produto toDomain(final AdminProdutoRequestDTO dto) {
        final Produto domain = new Produto();
        domain.setId(null);
        domain.setNome(dto.getNome());
        domain.setDescricao(dto.getDescricao());
        domain.setPrecoVenda(dto.getPreco());
        domain.setImagem(dto.getImagem());
        domain.setCategoria(dto.getCategoria());
        domain.setCodigoBarras(dto.getCodigoBarras());
        domain.setPrecoCusto(null);
        domain.setEstoque(dto.getEstoque());
        domain.setDisponivel(dto.getAtivo());
        domain.setFabricante(null);
        domain.setCodigoOriginal(null);
        domain.setUnidade(null);
        domain.setDataCadastro(LocalDateTime.now());
        return domain;
    }

    /**
     * Converts entity to response DTO.
     *
     * @param entity product entity
     * @return response DTO
     */
    private ProdutoResponseDTO toResponse(final ProdutoEntity entity) {
        final ProdutoResponseDTO dto = new ProdutoResponseDTO();

        final UUID publicId = entity.getId() != null
                ? UUID.nameUUIDFromBytes(
                        (PUBLIC_ID_PREFIX + entity.getId()).getBytes()
                )
                : UUID.randomUUID();

        dto.setEntityId(entity.getId());
        dto.setId(publicId);
        dto.setNome(nvl(entity.getNome(), "Produto"));
        dto.setDescricao(nvl(entity.getDescricao(), ""));
        dto.setPreco(entity.getPrecoVenda());
        dto.setImagem(entity.getImagem());
        dto.setCategoria(entity.getCategoria());
        dto.setEstoqueAtual(entity.getEstoque());
        dto.setValidade(LocalDate.now().plusYears(DEFAULT_VALIDADE_YEARS));
        dto.setCodigoBarras(entity.getCodigoBarras());
        dto.setMarca(entity.getFabricante());
        dto.setFornecedor(null);
        dto.setQuantidadeVendida(null);
        dto.setDataCadastro(
                entity.getDataCadastro() != null
                        ? entity.getDataCadastro().atStartOfDay()
                        : null
        );
        dto.setDataAtualizacao(entity.getUpdatedAt());
        final boolean produtoDestaque =
                Boolean.TRUE.equals(entity.getDestaqueCarrossel());
        dto.setProdutoDestaque(produtoDestaque);
        dto.setProdutoRecomendadoIA(Boolean.FALSE);
        dto.setProdutoControlado(Boolean.FALSE);
        dto.setAvaliacaoMedia(null);
        dto.setTags(null);

        final boolean ativo = Boolean.TRUE.equals(entity.getDisponivel())
                && entity.getPrecoVenda() != null
                && entity.getPrecoVenda().signum() > 0
                && entity.getEstoque() != null
                && entity.getEstoque() > 0;

        dto.setSituacao(
                ativo
                        ? ProdutoResponseDTO.SituacaoProduto.ATIVO
                        : ProdutoResponseDTO.SituacaoProduto.ESGOTADO
        );

        return dto;
    }

    /**
     * Validates image requirement for active products.
     *
     * @param dto request payload
     */
    private void ensureImageWhenActive(final AdminProdutoRequestDTO dto) {
        if (Boolean.TRUE.equals(dto.getAtivo())
                && (dto.getImagem() == null || dto.getImagem().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Imagem obrigatória para produtos disponibilizados na web."
            );
        }
    }

    /**
     * Validates unique product name in repository.
     *
     * @param dto request payload
     * @param currentId current product id for update scenario
     */
    private void ensureUniqueName(
            final AdminProdutoRequestDTO dto,
            final Long currentId
    ) {
        if (dto.getNome() == null) {
            return;
        }
        final String nome = dto.getNome().trim();
        if (nome.isBlank()) {
            return;
        }
        repo.findByNomeIgnoreCase(nome)
                .filter(existing -> currentId == null
                        || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            DUPLICATE_NAME_MESSAGE
                    );
                });
    }

    /**
     * Null-safe string helper.
     *
     * @param value source value
     * @param def default value
     * @return normalized value
     */
    private static String nvl(final String value, final String def) {
        return (value == null || value.isBlank()) ? def : value;
    }
}
