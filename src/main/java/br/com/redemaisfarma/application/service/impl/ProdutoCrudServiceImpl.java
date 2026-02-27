/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO
 *  br.com.redemaisfarma.domain.Produto
 *  jakarta.persistence.EntityNotFoundException
 *  lombok.Generated
 *  org.springframework.context.annotation.Profile
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO;
import br.com.redemaisfarma.application.service.ProdutoService;
import br.com.redemaisfarma.domain.Produto;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile(value={"legacy"})
@Transactional
public class ProdutoCrudServiceImpl
implements ProdutoService {
    private final ProdutoRepository repository;

    @Override
    @Transactional(readOnly=true)
    public Produto findById(Long id) {
        return this.repository.findById(id).map(this::toDomain).orElseThrow(() -> new EntityNotFoundException("Produto n\u00e3o encontrado: id=" + String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly=true)
    public List<Produto> list() {
        return this.repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Produto create(Produto produto) {
        ProdutoEntity entity = this.toEntity(produto);
        entity.setId(null);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setDataImportacao(LocalDateTime.now());
        return this.toDomain((ProdutoEntity)this.repository.save(entity));
    }

    @Override
    public Produto createFromDto(CadastroProdutoRequestDTO dto) {
        Produto d = new Produto();
        d.setId(null);
        d.setNome(dto.getNome());
        d.setDescricao(dto.getDescricao());
        d.setDataCadastro(LocalDateTime.now());
        return this.create(d);
    }

    @Override
    public Produto update(Long id, Produto produto) {
        ProdutoEntity entity = (ProdutoEntity)this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto n\u00e3o encontrado: id=" + String.valueOf(id)));
        if (produto.getNome() != null) {
            entity.setNome(produto.getNome());
        }
        if (produto.getDescricao() != null) {
            entity.setDescricao(produto.getDescricao());
        }
        if (produto.getCodigoBarras() != null) {
            entity.setCodigoBarras(produto.getCodigoBarras());
            entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        }
        if (produto.getPrecoVenda() != null) {
            entity.setPrecoVenda(produto.getPrecoVenda());
        }
        if (produto.getCategoria() != null) {
            entity.setCategoria(produto.getCategoria());
        }
        if (produto.getEstoque() != null) {
            entity.setEstoque(produto.getEstoque());
        }
        if (produto.getDisponivel() != null) {
            entity.setDisponivel(produto.getDisponivel());
        }
        if (produto.getFabricante() != null) {
            entity.setFabricante(produto.getFabricante());
        }
        if (produto.getImagem() != null) {
            entity.setImagem(produto.getImagem());
        }
        return this.toDomain((ProdutoEntity)this.repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!this.repository.existsById(id)) {
            throw new EntityNotFoundException("Produto n\u00e3o encontrado: id=" + String.valueOf(id));
        }
        this.repository.deleteById(id);
    }

    @Override
    public Produto validar(Long id, String validador) {
        ProdutoEntity entity = (ProdutoEntity)this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto n\u00e3o encontrado: id=" + String.valueOf(id)));
        entity.setStatus(ProdutoStatus.VALIDADO);
        entity.setValidador(validador);
        return this.toDomain((ProdutoEntity)this.repository.save(entity));
    }

    @Override
    public Produto publicar(Long id, String validador) {
        ProdutoEntity entity = (ProdutoEntity)this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto n\u00e3o encontrado: id=" + String.valueOf(id)));
        entity.setStatus(ProdutoStatus.PUBLICADO);
        entity.setValidador(validador);
        entity.setPublicadoEm(LocalDateTime.now());
        return this.toDomain((ProdutoEntity)this.repository.save(entity));
    }

    @Override
    @Transactional(readOnly=true)
    public List<Produto> listByStatus(ProdutoStatus status) {
        return this.repository.findByStatus(status, Pageable.unpaged()).stream().map(this::toDomain).toList();
    }

    private Produto toDomain(ProdutoEntity e) {
        if (e == null) {
            return null;
        }
        Produto d = new Produto();
        d.setId(e.getId());
        d.setNome(e.getNome());
        d.setDescricao(e.getDescricao());
        d.setCodigoBarras(e.getCodigoBarras());
        d.setPrecoVenda(e.getPrecoVenda());
        d.setCategoria(e.getCategoria());
        d.setEstoque(e.getEstoque());
        d.setDisponivel(e.getDisponivel());
        d.setImagem(e.getImagem());
        d.setFabricante(e.getFabricante());
        d.setDataCadastro(ProdutoCrudServiceImpl.toLocalDateTime(e.getDataCadastro()));
        return d;
    }

    private ProdutoEntity toEntity(Produto d) {
        if (d == null) {
            return null;
        }
        ProdutoEntity e = new ProdutoEntity();
        e.setId(d.getId());
        e.setNome(d.getNome());
        e.setDescricao(d.getDescricao());
        e.setCodigoBarras(d.getCodigoBarras());
        if (d.getCodigoBarras() != null && !d.getCodigoBarras().isBlank()) {
            e.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        }
        e.setPrecoVenda(d.getPrecoVenda());
        e.setCategoria(d.getCategoria());
        e.setEstoque(d.getEstoque());
        e.setDisponivel(d.getDisponivel());
        e.setImagem(d.getImagem());
        e.setFabricante(d.getFabricante());
        e.setDataCadastro(ProdutoCrudServiceImpl.toLocalDate(d.getDataCadastro()));
        return e;
    }

    private static LocalDate toLocalDate(LocalDateTime dt) {
        return dt != null ? dt.toLocalDate() : null;
    }

    private static LocalDateTime toLocalDateTime(LocalDate d) {
        return d != null ? d.atStartOfDay() : null;
    }

    @Generated
    public ProdutoCrudServiceImpl(ProdutoRepository repository) {
        this.repository = repository;
    }
}
