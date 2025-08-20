package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.domain.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela lógica de negócios dos produtos no novo sistema.
 */
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoJpaRepository produtoRepository;

    /**
     * Retorna todos os produtos cadastrados no novo banco.
     */
    public List<Produto> listarTodos() {
        return produtoRepository.findAll().stream().map(ProdutoMapper::toDomain).toList();
    }

    /**
     * Busca um produto pelo ID.
     *
     * @param id
     *            Identificador do produto
     *
     * @return Produto, se encontrado
     */
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id).map(ProdutoMapper::toDomain);
    }

    /**
     * Salva um novo produto no banco de dados.
     *
     * @param produto
     *            Produto a ser salvo
     *
     * @return Produto salvo
     */
    public Produto salvar(Produto produto) {
        var entidade = ProdutoMapper.toEntity(produto);
        var salvo = produtoRepository.save(entidade);
        return ProdutoMapper.toDomain(salvo);
    }

    /**
     * Atualiza um produto existente.
     *
     * @param id
     *            ID do produto a ser atualizado
     * @param dadosAtualizados
     *            Dados atualizados do produto
     *
     * @return Produto atualizado
     */
    public Produto atualizar(Long id, Produto dadosAtualizados) {
        return produtoRepository.findById(id).map(entity -> {
            entity.setNome(dadosAtualizados.getNome());
            entity.setDescricao(dadosAtualizados.getDescricao());
            entity.setPrecoVenda(dadosAtualizados.getPrecoVenda());
            entity.setImagem(dadosAtualizados.getImagem());
            entity.setCategoria(dadosAtualizados.getCategoria());
            entity.setCodigoBarras(dadosAtualizados.getCodigoBarras());
            entity.setPrecoCusto(dadosAtualizados.getPrecoCusto());
            entity.setEstoque(dadosAtualizados.getEstoque());
            entity.setDisponivel(dadosAtualizados.getDisponivel());
            entity.setFabricante(dadosAtualizados.getFabricante());
            entity.setUnidade(dadosAtualizados.getUnidade());
            return produtoRepository.save(entity);
        }).map(ProdutoMapper::toDomain).orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }

    /**
     * Remove um produto do banco.
     *
     * @param id
     *            ID do produto a ser removido
     */
    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }
}
