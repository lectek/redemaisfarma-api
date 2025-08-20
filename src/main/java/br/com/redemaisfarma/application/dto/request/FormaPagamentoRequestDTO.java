package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(name = "FormaPagamentoRequestDTO", description = "DTO para a forma de pagamento no momento da criação do pedido")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormaPagamentoRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // cartao_credito | cartao_debito | pix | boleto | dinheiro | carteira
    @Schema(description = "Tipo de pagamento escolhido", example = "cartao_credito", required = true)
    @NotBlank(message = "{formaPagamento.tipo.notBlank}")
    @Pattern(regexp = "^(cartao_credito|cartao_debito|pix|boleto|dinheiro|carteira)$", message = "{formaPagamento.tipo.pattern}")
    @Size(max = 30, message = "{formaPagamento.tipo.size}")
    @JsonProperty("tipoPagamento")
    private String tipoPagamento;

    @Schema(description = "Número de parcelas (se aplicável)", example = "3")
    @Min(value = 1, message = "{formaPagamento.parcelas.min}")
    @Max(value = 24, message = "{formaPagamento.parcelas.max}")
    @JsonProperty("parcelas")
    private Integer parcelas;

    @Schema(description = "Valor total desta forma de pagamento", example = "150.00", required = true)
    @NotNull(message = "{formaPagamento.valor.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{formaPagamento.valor.min}")
    @Digits(integer = 12, fraction = 2, message = "{formaPagamento.valor.digits}")
    @JsonProperty("valor")
    private BigDecimal valor;

    @Schema(description = "Bandeira do cartão (se cartão)", example = "VISA")
    @Size(max = 30, message = "{formaPagamento.bandeira.size}")
    @JsonProperty("bandeira")
    private String bandeira;

    // --------------------- Validações compostas (regras de negócio) ---------------------

    /**
     * Parcelas obrigatórias para cartao_credito; proibidas (ou 1) para PIX/dinheiro/debito/carteira; boleto opcional.
     */
    @AssertTrue(message = "{formaPagamento.parcelas.coerencia}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isParcelasCoerentes() {
        if (tipoPagamento == null || valor == null || valor.signum() <= 0)
            return false;

        switch (tipoPagamento) {
        case "cartao_credito":
            // precisa informar parcelas entre 1..24
            return parcelas != null && parcelas >= 1 && parcelas <= 24;
        case "pix":
        case "dinheiro":
        case "cartao_debito":
        case "carteira":
            // sem parcelamento; aceitar null ou 1
            return parcelas == null || parcelas == 1;
        case "boleto":
            // pode ser à vista (null/1) ou parcelado (1..24) se sua regra permitir
            return parcelas == null || (parcelas >= 1 && parcelas <= 24);
        default:
            return false;
        }
    }

    /** Bandeira obrigatória apenas para cartões; proibida nos demais. */
    @AssertTrue(message = "{formaPagamento.bandeira.coerencia}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isBandeiraCoerente() {
        if (tipoPagamento == null)
            return false;
        boolean isCartao = "cartao_credito".equals(tipoPagamento) || "cartao_debito".equals(tipoPagamento);
        if (isCartao) {
            return bandeira != null && !bandeira.isBlank();
        } else {
            return bandeira == null || bandeira.isBlank();
        }
    }

    // --------------------- Construtores ---------------------

    public FormaPagamentoRequestDTO() {
    }

    public FormaPagamentoRequestDTO(String tipoPagamento, Integer parcelas, BigDecimal valor, String bandeira) {
        this.tipoPagamento = tipoPagamento;
        this.parcelas = parcelas;
        this.valor = valor;
        this.bandeira = bandeira;
    }

    // --------------------- Getters/Setters ---------------------

    public String getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    // --------------------- utilitários ---------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FormaPagamentoRequestDTO that))
            return false;
        return Objects.equals(tipoPagamento, that.tipoPagamento) && Objects.equals(parcelas, that.parcelas)
                && Objects.equals(valor, that.valor) && Objects.equals(bandeira, that.bandeira);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoPagamento, parcelas, valor, bandeira);
    }

    @Override
    public String toString() {
        return "FormaPagamentoRequestDTO{" + "tipoPagamento='" + tipoPagamento + '\'' + ", parcelas=" + parcelas
                + ", valor=" + valor + ", bandeira='" + bandeira + '\'' + '}';
    }
}
