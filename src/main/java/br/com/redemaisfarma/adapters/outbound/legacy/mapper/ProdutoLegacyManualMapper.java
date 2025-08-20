package br.com.redemaisfarma.adapters.outbound.legacy.mapper;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class ProdutoLegacyManualMapper {

    public ProdutoEntity toEntity(ProdutoLegacyEntity legacy) {
        if (legacy == null) {
            return null;
        }

        ProdutoEntity entity = new ProdutoEntity();

        entity.setId(null); // 🔄 Sempre nulo para novo registro (evita sobrescrever)

        entity.setNome(legacy.getNome());
        entity.setDescricao(legacy.getNome());
        entity.setPrecoVenda(legacy.getPrecoVenda() != null ? BigDecimal.valueOf(legacy.getPrecoVenda()) : null);
        entity.setPrecoCusto(legacy.getPrecoPromocao() != null ? BigDecimal.valueOf(legacy.getPrecoPromocao()) : null);
        entity.setCategoria("Sem Categoria");

        entity.setEstoque(legacy.getSaldo() != null ? legacy.getSaldo().intValue() : 0);

        entity.setDisponivel(true);

        entity.setFabricante(
                legacy.getMargemLucro() != null ? "Lucro: " + legacy.getMargemLucro() + "%" : "Desconhecido");

        if (legacy.getInicioPromocao() != null) {
            entity.setDataCadastro(legacy.getInicioPromocao().toLocalDate());
        } else {
            entity.setDataCadastro(LocalDate.now());
        }

        entity.setImagem(null);

        return entity;
    }
}
