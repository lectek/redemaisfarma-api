package br.com.redemaisfarma.adapters.outbound.legacy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um produto vindo do sistema legado (Firebird).
 * Mapeia os campos mais comuns da tabela PRODUTOS no Digifarma.
 */
public record LegacyProdutoDTO(
        Long legacyId,
        String nome,
        String apresentacao,
        String ean,
        BigDecimal precoVenda,
        BigDecimal precoPromocional,
        Integer estoque,
        LocalDateTime updatedAt
) {}
