// src/main/java/br/com/redemaisfarma/application/mapper/ProdutoRestMapper.java
package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;

import java.util.List;
import java.util.UUID;

public class ProdutoRestMapper {

    public static ProdutoResponseDTO toResponse(ProdutoEntity e) {
        if (e == null) return null;

        // UUID público estável derivado do ID numérico interno
        UUID publicId = (e.getId() != null)
                ? UUID.nameUUIDFromBytes(("produto:" + e.getId()).getBytes())
                : UUID.randomUUID();

        // Situação: ATIVO apenas se disponível + preço > 0 + estoque > 0
        boolean ativo = Boolean.TRUE.equals(e.getDisponivel())
                && e.getPrecoVenda() != null && e.getPrecoVenda().signum() > 0
                && e.getEstoque() > 0;

        ProdutoResponseDTO.SituacaoProduto situacao = ativo
                ? ProdutoResponseDTO.SituacaoProduto.ATIVO
                : ProdutoResponseDTO.SituacaoProduto.ESGOTADO;

        // Constrói DTO (sem entityId no construtor)
        ProdutoResponseDTO dto = new ProdutoResponseDTO(
                publicId,                                // id (UUID público)
                nvl(e.getNome(), "Produto"),             // nome
                nvl(e.getDescricao(), ""),               // descricao
                e.getPrecoVenda(),                       // preco
                e.getImagem(),                           // imagem
                e.getCategoria(),                        // categoria
                e.getEstoque(),                          // estoqueAtual
                null,                                    // validade (quando houver)
                e.getCodigoBarras(),                     // codigoBarras
                e.getFabricante(),                       // marca
                null,                                    // fornecedor (a definir quando existir)
                null,                                    // quantidadeVendida (quando houver)
                e.getDataCadastro() != null ? e.getDataCadastro().atStartOfDay() : null, // dataCadastro
                e.getUpdatedAt(),                        // dataAtualizacao
                Boolean.TRUE.equals(e.getDestaqueCarrossel()), // produtoDestaque
                Boolean.FALSE,                           // produtoRecomendadoIA (placeholder)
                Boolean.FALSE,                           // produtoControlado (placeholder)
                null,                                    // avaliacaoMedia (quando houver)
                List.of(),                               // tags (quando houver)
                situacao                                 // situacao
        );

        // Preenche o ID interno da entidade (chave do banco)
        dto.setEntityId(e.getId());

        return dto;
    }

    public static List<ProdutoResponseDTO> toResponseList(List<ProdutoEntity> entities) {
        return entities.stream().map(ProdutoRestMapper::toResponse).toList();
    }

    private static String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
        }
}
