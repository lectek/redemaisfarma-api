/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.constraints.AssertTrue
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Digits
 *  jakarta.validation.constraints.Max
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(name="FormaPagamentoRequestDTO", description="DTO para a forma de pagamento no momento da cria\u00e7\u00e3o do pedido")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class FormaPagamentoRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="Tipo de pagamento escolhido", example="cartao_credito", required=true)
    @NotBlank(message="{formaPagamento.tipo.notBlank}")
    @Pattern(regexp="^(cartao_credito|cartao_debito|pix|boleto|dinheiro|carteira)$", message="{formaPagamento.tipo.pattern}")
    @Size(max=30, message="{formaPagamento.tipo.size}")
    @JsonProperty(value="tipoPagamento")
    private @NotBlank(message="{formaPagamento.tipo.notBlank}") @Pattern(regexp="^(cartao_credito|cartao_debito|pix|boleto|dinheiro|carteira)$", message="{formaPagamento.tipo.pattern}") @Size(max=30, message="{formaPagamento.tipo.size}") String tipoPagamento;
    @Schema(description="N\u00famero de parcelas (se aplic\u00e1vel)", example="3")
    @Min(value=1L, message="{formaPagamento.parcelas.min}")
    @Max(value=24L, message="{formaPagamento.parcelas.max}")
    @JsonProperty(value="parcelas")
    private @Min(value=1L, message="{formaPagamento.parcelas.min}") @Max(value=24L, message="{formaPagamento.parcelas.max}") Integer parcelas;
    @Schema(description="Valor total desta forma de pagamento", example="150.00", required=true)
    @NotNull(message="{formaPagamento.valor.notNull}")
    @DecimalMin(value="0.01", inclusive=true, message="{formaPagamento.valor.min}")
    @Digits(integer=12, fraction=2, message="{formaPagamento.valor.digits}")
    @JsonProperty(value="valor")
    private @NotNull(message="{formaPagamento.valor.notNull}") @DecimalMin(value="0.01", inclusive=true, message="{formaPagamento.valor.min}") @Digits(integer=12, fraction=2, message="{formaPagamento.valor.digits}") BigDecimal valor;
    @Schema(description="Bandeira do cart\u00e3o (se cart\u00e3o)", example="VISA")
    @Size(max=30, message="{formaPagamento.bandeira.size}")
    @JsonProperty(value="bandeira")
    private @Size(max=30, message="{formaPagamento.bandeira.size}") String bandeira;

    @AssertTrue(message="{formaPagamento.parcelas.coerencia}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{formaPagamento.parcelas.coerencia}") boolean isParcelasCoerentes() {
        if (this.tipoPagamento == null || this.valor == null || this.valor.signum() <= 0) {
            return false;
        }
        switch (this.tipoPagamento) {
            case "cartao_credito": {
                return this.parcelas != null && this.parcelas >= 1 && this.parcelas <= 24;
            }
            case "pix": 
            case "dinheiro": 
            case "cartao_debito": 
            case "carteira": {
                return this.parcelas == null || this.parcelas == 1;
            }
            case "boleto": {
                return this.parcelas == null || this.parcelas >= 1 && this.parcelas <= 24;
            }
        }
        return false;
    }

    @AssertTrue(message="{formaPagamento.bandeira.coerencia}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{formaPagamento.bandeira.coerencia}") boolean isBandeiraCoerente() {
        boolean isCartao;
        if (this.tipoPagamento == null) {
            return false;
        }
        boolean bl = isCartao = "cartao_credito".equals(this.tipoPagamento) || "cartao_debito".equals(this.tipoPagamento);
        if (isCartao) {
            return this.bandeira != null && !this.bandeira.isBlank();
        }
        return this.bandeira == null || this.bandeira.isBlank();
    }

    public FormaPagamentoRequestDTO() {
    }

    public FormaPagamentoRequestDTO(String tipoPagamento, Integer parcelas, BigDecimal valor, String bandeira) {
        this.tipoPagamento = tipoPagamento;
        this.parcelas = parcelas;
        this.valor = valor;
        this.bandeira = bandeira;
    }

    public String getTipoPagamento() {
        return this.tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public Integer getParcelas() {
        return this.parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getBandeira() {
        return this.bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FormaPagamentoRequestDTO)) {
            return false;
        }
        FormaPagamentoRequestDTO that = (FormaPagamentoRequestDTO)o;
        return Objects.equals(this.tipoPagamento, that.tipoPagamento) && Objects.equals(this.parcelas, that.parcelas) && Objects.equals(this.valor, that.valor) && Objects.equals(this.bandeira, that.bandeira);
    }

    public int hashCode() {
        return Objects.hash(this.tipoPagamento, this.parcelas, this.valor, this.bandeira);
    }

    public String toString() {
        return "FormaPagamentoRequestDTO{tipoPagamento='" + this.tipoPagamento + "', parcelas=" + this.parcelas + ", valor=" + String.valueOf(this.valor) + ", bandeira='" + this.bandeira + "'}";
    }
}

