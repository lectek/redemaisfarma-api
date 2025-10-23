// src/main/java/br/com/redemaisfarma/application/service/impl/ProdutoCrudServiceImpl.java
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO;
import br.com.redemaisfarma.application.service.ProdutoService;
import br.com.redemaisfarma.domain.Produto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("legacy") // ativa apenas no profile legacy
@RequiredArgsConstructor
@Transactional
public class ProdutoCrudServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;

    // ---- CRUD básico ----
    @Override @Transactional(readOnly = true)
    public Produto findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: id=" + id));
    }

    @Override @Transactional(readOnly = true)
    public List<Produto> list() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Produto create(Produto produto) {
        ProdutoEntity entity = toEntity(produto);
        entity.setId(null);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setDataImportacao(LocalDateTime.now());
        return toDomain(repository.save(entity));
    }

    @Override
    public Produto createFromDto(CadastroProdutoRequestDTO dto) {
        Produto d = new Produto();
        d.setId(null);
        d.setNome(dto.getNome());
        d.setDescricao(dto.getDescricao());
        d.setDataCadastro(LocalDateTime.now());
        return create(d);
    }

    @Override
    public Produto update(Long id, Produto produto) {
        ProdutoEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: id=" + id));

        if (produto.getNome() != null)         entity.setNome(produto.getNome());
        if (produto.getDescricao() != null)    entity.setDescricao(produto.getDescricao());
        if (produto.getCodigoBarras() != null) entity.setCodigoBarras(produto.getCodigoBarras());
        if (produto.getPrecoVenda() != null)   entity.setPrecoVenda(produto.getPrecoVenda());
        if (produto.getCategoria() != null)    entity.setCategoria(produto.getCategoria());
        if (produto.getEstoque() != null)      entity.setEstoque(produto.getEstoque());
        if (produto.getDisponivel() != null)   entity.setDisponivel(produto.getDisponivel());
        if (produto.getFabricante() != null)   entity.setFabricante(produto.getFabricante());
        if (produto.getImagem() != null)       entity.setImagem(produto.getImagem());

        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("Produto não encontrado: id=" + id);
        repository.deleteById(id);
    }

    // ---- Fluxo de validação/publicação ----
    @Override
    public Produto validar(Long id, String validador) {
        ProdutoEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: id=" + id));
        entity.setStatus(ProdutoStatus.VALIDADO);
        entity.setValidador(validador);
        return toDomain(repository.save(entity));
    }

    @Override
    public Produto publicar(Long id, String validador) {
        ProdutoEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: id=" + id));
        entity.setStatus(ProdutoStatus.PUBLICADO);
        entity.setValidador(validador);
        entity.setPublicadoEm(LocalDateTime.now());
        return toDomain(repository.save(entity));
    }

    @Override @Transactional(readOnly = true)
    public List<Produto> listByStatus(ProdutoStatus status) {
        return repository.findByStatus(status, org.springframework.data.domain.Pageable.unpaged())
                .stream().map(this::toDomain).toList();
    }

    // ---- Converters ----
    private Produto toDomain(ProdutoEntity e) {
        if (e == null) return null;
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
        d.setDataCadastro(toLocalDateTime(e.getDataCadastro()));
        return d;
    }

    private ProdutoEntity toEntity(Produto d) {
        if (d == null) return null;
        ProdutoEntity e = new ProdutoEntity();
        e.setId(d.getId());
        e.setNome(d.getNome());
        e.setDescricao(d.getDescricao());
        e.setCodigoBarras(d.getCodigoBarras());
        e.setPrecoVenda(d.getPrecoVenda());
        e.setCategoria(d.getCategoria());
        e.setEstoque(d.getEstoque());
        e.setDisponivel(d.getDisponivel());
        e.setImagem(d.getImagem());
        e.setFabricante(d.getFabricante());
        e.setDataCadastro(toLocalDate(d.getDataCadastro()));
        return e;
    }

    private static LocalDate toLocalDate(LocalDateTime dt) {
        return dt != null ? dt.toLocalDate() : null;
    }

    private static LocalDateTime toLocalDateTime(LocalDate d) {
        return d != null ? d.atStartOfDay() : null;
    }
}
