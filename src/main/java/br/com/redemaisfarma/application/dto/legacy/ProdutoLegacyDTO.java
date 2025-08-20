package br.com.redemaisfarma.application.dto.legacy;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transportar dados dos produtos do banco legado (Firebird) sem expor diretamente a entidade JPA.
 */
@Getter
@Setter
@NoArgsConstructor(force = true) // ✅ Necessário para MapStruct com campos 'final'
@AllArgsConstructor
@Builder
public class ProdutoLegacyDTO {

    private final Integer id;
    private final String nome;
    private final String codigoBarras;
    private final Float saldo;
    private final Float precoVenda;
    private final Float precoPromocao;
    private final Float estoqueMinimo;
    private final Float margemLucro;
    private final LocalDateTime inicioPromocao;
    private final LocalDateTime terminoPromocao;
    private final BigDecimal bonus;
    private final String apresentacao;
    private final Float precoAnterior;
    private final Integer fornecedorId;
    private final Integer categoriaId;
    private final Integer comissaoId;
}
