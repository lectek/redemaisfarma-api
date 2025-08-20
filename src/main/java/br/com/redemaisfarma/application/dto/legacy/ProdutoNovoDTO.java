package br.com.redemaisfarma.application.dto.legacy;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoNovoDTO {

    private Long id;
    private String descricaoProd;
    private String codigoBarras;
    private String unidade;
    private String categoria;
    private String nome;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private BigDecimal precoPromocao; // 🔄 novo campo vindo do legado
    private BigDecimal bonus;
    private int estoqueAtual; // mapeado de saldo
    private BigDecimal margemLucro; // 🔄 novo campo
    private Float precoAnterior;
    private Boolean ativo;
    private String fornecedor;
    private String fabricante;
    private LocalDate dataCadastro;
    private String imagem;
    private LocalDateTime inicioPromocao; // 🔄 vindo do legado
    private LocalDateTime terminoPromocao; // 🔄 vindo do legado
}
