package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.domain.Produto;

public class ProdutoMapper {

    public static Produto toDomain(ProdutoEntity entity) {
        if (entity == null)
            return null;

        Produto produto = new Produto();
        produto.setId(entity.getId());
        produto.setNome(entity.getNome());
        produto.setDescricao(entity.getDescricao());
        produto.setPrecoVenda(entity.getPrecoVenda());
        produto.setImagem(entity.getImagem());
        produto.setCategoria(entity.getCategoria());
        produto.setCodigoBarras(entity.getCodigoBarras());
        produto.setPrecoCusto(entity.getPrecoCusto());
        produto.setEstoque(entity.getEstoque());
        produto.setDisponivel(entity.getDisponivel());
        produto.setFabricante(entity.getFabricante());

        produto.setUnidade(entity.getUnidade());

        return produto;
    }

    public static ProdutoEntity toEntity(Produto domain) {
        if (domain == null)
            return null;

        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setDescricao(domain.getDescricao());
        entity.setPrecoVenda(domain.getPrecoVenda());
        entity.setImagem(domain.getImagem());
        entity.setCategoria(domain.getCategoria());
        entity.setCodigoBarras(domain.getCodigoBarras());
        entity.setPrecoCusto(domain.getPrecoCusto());
        entity.setEstoque(domain.getEstoque());
        entity.setDisponivel(domain.getDisponivel());
        entity.setFabricante(domain.getFabricante());

        entity.setUnidade(domain.getUnidade());

        return entity;
    }
}
