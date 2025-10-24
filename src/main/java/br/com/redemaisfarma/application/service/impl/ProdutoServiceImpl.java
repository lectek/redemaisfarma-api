/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO
 *  br.com.redemaisfarma.application.mapper.ProdutoMapper
 *  br.com.redemaisfarma.domain.Produto
 *  lombok.Generated
 *  org.springframework.context.annotation.Primary
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.application.service.ProdutoService;
import br.com.redemaisfarma.domain.Produto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Generated;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
public class ProdutoServiceImpl
implements ProdutoService {
    private final ProdutoJpaRepository repo;

    @Override
    @Transactional(readOnly=true)
    public Produto findById(Long id) {
        ProdutoEntity e = (ProdutoEntity)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto n\u00e3o encontrado: " + String.valueOf(id)));
        return ProdutoMapper.toDomain((ProdutoEntity)e);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Produto> list() {
        return this.repo.findAll().stream().map(ProdutoMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Produto create(Produto produto) {
        ProdutoEntity e = ProdutoMapper.toEntity((Produto)produto);
        e.setId(null);
        e.setStatus(ProdutoStatus.IMPORTADO);
        e.setDataImportacao(LocalDateTime.now());
        ProdutoEntity salvo = (ProdutoEntity)this.repo.save(e);
        return ProdutoMapper.toDomain((ProdutoEntity)salvo);
    }

    @Override
    @Transactional
    public Produto update(Long id, Produto produto) {
        ProdutoEntity atual = (ProdutoEntity)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto n\u00e3o encontrado: " + String.valueOf(id)));
        ProdutoMapper.updateEntity((ProdutoEntity)atual, (Produto)produto);
        ProdutoEntity salvo = (ProdutoEntity)this.repo.save(atual);
        return ProdutoMapper.toDomain((ProdutoEntity)salvo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!this.repo.existsById(id)) {
            throw new IllegalArgumentException("Produto n\u00e3o encontrado: " + String.valueOf(id));
        }
        this.repo.deleteById(id);
    }

    @Override
    @Transactional
    public Produto createFromDto(CadastroProdutoRequestDTO dto) {
        Produto domain = new Produto();
        domain.setId(null);
        domain.setNome(dto.getNome());
        domain.setDescricao(dto.getDescricao());
        domain.setPrecoVenda(dto.getPrecoVenda());
        domain.setCategoria(dto.getCategoria());
        domain.setDataCadastro(LocalDateTime.now());
        ProdutoEntity e = ProdutoMapper.toEntity((Produto)domain);
        e.setId(null);
        e.setStatus(ProdutoStatus.IMPORTADO);
        e.setDataImportacao(LocalDateTime.now());
        ProdutoEntity salvo = (ProdutoEntity)this.repo.save(e);
        return ProdutoMapper.toDomain((ProdutoEntity)salvo);
    }

    @Override
    @Transactional
    public Produto validar(Long id, String validador) {
        ProdutoEntity entity = (ProdutoEntity)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto n\u00e3o encontrado: " + String.valueOf(id)));
        entity.setStatus(ProdutoStatus.VALIDADO);
        entity.setValidador(validador);
        ProdutoEntity salvo = (ProdutoEntity)this.repo.save(entity);
        return ProdutoMapper.toDomain((ProdutoEntity)salvo);
    }

    @Override
    @Transactional
    public Produto publicar(Long id, String validador) {
        ProdutoEntity entity = (ProdutoEntity)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto n\u00e3o encontrado: " + String.valueOf(id)));
        entity.setStatus(ProdutoStatus.PUBLICADO);
        entity.setValidador(validador);
        entity.setPublicadoEm(LocalDateTime.now());
        ProdutoEntity salvo = (ProdutoEntity)this.repo.save(entity);
        return ProdutoMapper.toDomain((ProdutoEntity)salvo);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Produto> listByStatus(ProdutoStatus status) {
        return this.repo.findByStatus(status, Pageable.unpaged()).stream().map(ProdutoMapper::toDomain).toList();
    }

    @Generated
    public ProdutoServiceImpl(ProdutoJpaRepository repo) {
        this.repo = repo;
    }
}

