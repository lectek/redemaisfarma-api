package br.com.redemaisfarma.adapters.outbound.legacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUTOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoLegacyEntity {

    @Id
    @Column(name = "PRODUTO_ID", nullable = false)
    private Integer id;

    @Column(name = "PRODUTO", length = 100)
    private String nome;

    @Column(name = "COD_BARRAS", length = 20)
    private String codigoBarras;

    @Column(name = "PROD_SALDO")
    private Float saldo;

    @Column(name = "PROD_PRVENDA")
    private Float precoVenda;

    @Column(name = "PROD_PRPROMOCAO")
    private Float precoPromocao;

    @Column(name = "PROD_ESTMINIMO")
    private Float estoqueMinimo;

    @Column(name = "MARGEM_LUCRO")
    private Float margemLucro;

    @Column(name = "PROD_VIGENCIA")
    private LocalDateTime inicioPromocao;

    @Column(name = "PROM_ACABANDO")
    private LocalDateTime terminoPromocao;

    @Column(name = "BONUS", precision = 10, scale = 2)
    private BigDecimal bonus;

    @Column(name = "APRESENTACAO", length = 50)
    private String apresentacao;

    @Column(name = "PRECO_ANTERIOR")
    private Float precoAnterior;

    @Column(name = "FORNECEDOR_ID")
    private Integer fornecedorId;

    @Column(name = "CATEGORIA_ID")
    private Integer categoriaId;

    @Column(name = "PADRAO_COMISSAO_ID")
    private Integer comissaoId;
}
