/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  lombok.Generated
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.util.StringUtils
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.multipart.MultipartFile
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.dto.request.ProdutoRequestDTO;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.domain.Produto;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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

@RestController
@RequestMapping(value={"/api/admin/produtos"})
public class ProdutoAdminRestController {
    private final ProdutoJpaRepository repo;

    @GetMapping
    public Page<ProdutoResponseDTO> list(@RequestParam(required=false) String q, Pageable pageable) {
        return this.repo.searchPage(q, pageable).map(this::toResponse);
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<ProdutoResponseDTO> get(@PathVariable Long id) {
        return this.repo.findById(id).map(this::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> create(@Valid @RequestBody ProdutoRequestDTO dto) {
        Produto domain = this.toDomain(dto);
        ProdutoEntity entity = ProdutoMapper.toEntity(domain);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setDataImportacao(LocalDateTime.now());
        ProdutoEntity salvo = (ProdutoEntity)this.repo.save(entity);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body((Object)this.toResponse(salvo));
    }

    @PutMapping(value={"/{id}"})
    public ResponseEntity<ProdutoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO dto) {
        return this.repo.findById(id).map(atual -> {
            Produto src = this.toDomain(dto);
            ProdutoMapper.updateEntity(atual, src);
            atual.setUpdatedAt(LocalDateTime.now());
            ProdutoEntity salvo = (ProdutoEntity)this.repo.save(atual);
            return ResponseEntity.ok((Object)this.toResponse(salvo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value={"/{id}"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!this.repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        this.repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value={"/{id}/imagem"}, consumes={"multipart/form-data"})
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam(value="file") MultipartFile file) {
        return this.repo.findById(id).map(entity -> {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body((Object)"Arquivo vazio");
            }
            String filename = "produto-" + id + "-" + StringUtils.cleanPath((String)file.getOriginalFilename());
            entity.setImagem("/media/products/" + filename);
            entity.setUpdatedAt(LocalDateTime.now());
            this.repo.save(entity);
            return ResponseEntity.ok((Object)entity.getImagem());
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value={"/{id}/validar"})
    public ResponseEntity<ProdutoResponseDTO> validar(@PathVariable Long id, @RequestParam String validador) {
        return this.repo.findById(id).map(entity -> {
            entity.setStatus(ProdutoStatus.VALIDADO);
            entity.setValidador(validador);
            entity.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok((Object)this.toResponse((ProdutoEntity)this.repo.save(entity)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value={"/{id}/publicar"})
    public ResponseEntity<ProdutoResponseDTO> publicar(@PathVariable Long id, @RequestParam String validador) {
        return this.repo.findById(id).map(entity -> {
            entity.setStatus(ProdutoStatus.PUBLICADO);
            entity.setValidador(validador);
            entity.setPublicadoEm(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok((Object)this.toResponse((ProdutoEntity)this.repo.save(entity)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Produto toDomain(ProdutoRequestDTO dto) {
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
        UUID publicId = e.getId() != null ? UUID.nameUUIDFromBytes(("produto:" + e.getId()).getBytes()) : UUID.randomUUID();
        dto.setId(publicId);
        dto.setNome(ProdutoAdminRestController.nvl(e.getNome(), "Produto"));
        dto.setDescricao(ProdutoAdminRestController.nvl(e.getDescricao(), ""));
        dto.setPreco(e.getPrecoVenda());
        dto.setImagem(e.getImagem());
        dto.setCategoria(e.getCategoria());
        dto.setEstoqueAtual(e.getEstoque());
        dto.setValidade(LocalDate.now().plusYears(1L));
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
        boolean ativo = Boolean.TRUE.equals(e.getDisponivel()) && e.getPrecoVenda() != null && e.getPrecoVenda().signum() > 0 && e.getEstoque() > 0;
        dto.setSituacao(ativo ? ProdutoResponseDTO.SituacaoProduto.ATIVO : ProdutoResponseDTO.SituacaoProduto.ESGOTADO);
        return dto;
    }

    private static String nvl(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    @Generated
    public ProdutoAdminRestController(ProdutoJpaRepository repo) {
        this.repo = repo;
    }
}

