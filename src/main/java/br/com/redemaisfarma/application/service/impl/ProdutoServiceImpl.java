package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.application.service.ProdutoService;
import br.com.redemaisfarma.domain.Produto;
import lombok.Generated;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Primary
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoJpaRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Produto findById(Long id) {
        ProdutoEntity e = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        return ProdutoMapper.toDomain(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> list() {
        return repo.findAll().stream()
                .map(ProdutoMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Produto create(Produto produto) {
        ProdutoEntity e = ProdutoMapper.toEntity(produto);
        e.setId(null);
        if (e.getStatus() == null) e.setStatus(ProdutoStatus.IMPORTADO);
        if (e.getDataImportacao() == null) e.setDataImportacao(LocalDateTime.now());

        ProdutoEntity salvo = repo.save(e);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public Produto update(Long id, Produto produto) {
        ProdutoEntity atual = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        ProdutoMapper.updateEntity(atual, produto);
        ProdutoEntity salvo = repo.save(atual);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Produto não encontrado: " + id);
        }
        repo.deleteById(id);
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

        ProdutoEntity e = ProdutoMapper.toEntity(domain);
        e.setId(null);
        if (e.getStatus() == null) e.setStatus(ProdutoStatus.IMPORTADO);
        e.setDataImportacao(LocalDateTime.now());

        ProdutoEntity salvo = repo.save(e);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public Produto validar(Long id, String validador) {
        ProdutoEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        entity.setStatus(ProdutoStatus.VALIDADO);
        entity.setValidador(validador);

        ProdutoEntity salvo = repo.save(entity);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public Produto publicar(Long id, String validador) {
        ProdutoEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        entity.setStatus(ProdutoStatus.PUBLICADO);
        entity.setValidador(validador);
        entity.setPublicadoEm(LocalDateTime.now());

        ProdutoEntity salvo = repo.save(entity);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listByStatus(ProdutoStatus status) {
        return repo.findByStatus(status, Pageable.unpaged()).stream()
                .map(ProdutoMapper::toDomain)
                .toList();
    }

    @Generated
    public ProdutoServiceImpl(ProdutoJpaRepository repo) {
        this.repo = repo;
    }
}
