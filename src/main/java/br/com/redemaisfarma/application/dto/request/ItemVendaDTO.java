package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO que representa um item de venda na API RedeMaisFarma. - Descontos por percentual OU valor (ambos opcionais, mas
 * coerentes) - Total calculado pelo servidor (somente leitura) para evitar divergências - Validações para prevenir
 * números negativos e estouros
 */
@Schema(name = "ItemVendaDTO", description = "Detalhes de um item na venda")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemVendaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do produto", example = "456", required = true)
    @NotNull(message = "{itemVenda.produtoId.notNull}")
    @Min(value = 1, message = "{itemVenda.produtoId.min}")
    @JsonProperty("produtoId")
    private Long produtoId;

    @Schema(description = "Quantidade de unidades vendidas", example = "2", required = true)
    @NotNull(message = "{itemVenda.quantidade.notNull}")
    @Min(value = 1, message = "{itemVenda.quantidade.min}")
    @JsonProperty("quantidade")
    private Integer quantidade;

    @Schema(description = "Preço unitário do produto", example = "12.50", required = true)
    @NotNull(message = "{itemVenda.precoUnitario.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{itemVenda.precoUnitario.min}")
    @Digits(integer = 12, fraction = 2, message = "{itemVenda.precoUnitario.digits}")
    @JsonProperty("precoUnitario")
    private BigDecimal precoUnitario;

    @Schema(description = "Percentual de desconto (0-100)", example = "5.00")
    @DecimalMin(value = "0.00", inclusive = true, message = "{itemVenda.percentualDesconto.min}")
    @DecimalMax(value = "100.00", inclusive = true, message = "{itemVenda.percentualDesconto.max}")
    @Digits(integer = 3, fraction = 2, message = "{itemVenda.percentualDesconto.digits}")
    @JsonProperty("percentualDesconto")
    private BigDecimal percentualDesconto;

    @Schema(description = "Valor do desconto aplicado ao item", example = "0.62")
    @DecimalMin(value = "0.00", inclusive = true, message = "{itemVenda.valorDesconto.min}")
    @Digits(integer = 12, fraction = 2, message = "{itemVenda.valorDesconto.digits}")
    @JsonProperty("valorDesconto")
    private BigDecimal valorDesconto;

    @Schema(description = "Valor total após desconto (calculado pelo servidor)", example = "23.38", accessMode = Schema.AccessMode.READ_ONLY)
    @Digits(integer = 12, fraction = 2, message = "{itemVenda.total.digits}")
    @JsonProperty(value = "total", access = JsonProperty.Access.READ_ONLY)
    private BigDecimal total;

    @Schema(description = "Código do lote do produto", example = "Lote1234")
    @Size(max = 50, message = "{itemVenda.lote.size}")
    @JsonProperty("lote")
    private String lote;

    @Schema(description = "Data de validade do produto (opcional)", type = "string", format = "date-time", example = "2025-12-31T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("validade")
    private LocalDateTime validade;

    @Schema(description = "ID do tenant", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{itemVenda.tenantId.notBlank}")
    @Size(max = 100, message = "{itemVenda.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    // ------------------------------------------------------------------------------------
    // Regras de coerência (validações compostas)
    // ------------------------------------------------------------------------------------

    @AssertTrue(message = "{itemVenda.desconto.coerencia}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isDescontoCoerente() {
        if (quantidade == null || precoUnitario == null || precoUnitario.signum() <= 0 || quantidade < 1) {
            return false;
        }

        BigDecimal qtd = BigDecimal.valueOf(quantidade);
        BigDecimal bruto = precoUnitario.multiply(qtd);

        BigDecimal perc = percentualDesconto == null ? null : percentualDesconto;
        BigDecimal val = valorDesconto == null ? null : valorDesconto;

        // nenhum desconto é válido
        if (perc == null && val == null)
            return true;

        // percentual inválido
        if (perc != null && (perc.signum() < 0 || perc.compareTo(new BigDecimal("100.00")) > 0))
            return false;

        // valor inválido
        if (val != null && val.signum() < 0)
            return false;

        // se ambos informados, precisam ser equivalentes (tolerância de 1 centavo)
        if (perc != null && val != null) {
            BigDecimal esperado = bruto.multiply(perc).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            return esperado.subtract(val).abs().compareTo(new BigDecimal("0.01")) <= 0 && val.compareTo(bruto) <= 0;
        }

        // se apenas percentual
        if (perc != null) {
            BigDecimal esperado = bruto.multiply(perc).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            return esperado.compareTo(bruto) <= 0;
        }

        // se apenas valor
        return val != null && val.compareTo(bruto) <= 0;

    }

    // ------------------------------------------------------------------------------------
    // Construtores
    // ------------------------------------------------------------------------------------

    public ItemVendaDTO() {
    }

    public ItemVendaDTO(Long produtoId, Integer quantidade, BigDecimal precoUnitario, BigDecimal percentualDesconto,
            BigDecimal valorDesconto, BigDecimal total, String lote, LocalDateTime validade, String tenantId,
            UUID traceId) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.percentualDesconto = percentualDesconto;
        this.valorDesconto = valorDesconto;
        this.total = total; // será ignorado na criação (servidor recalcula), mas mantido para eventual leitura
        this.lote = lote;
        this.validade = validade;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    // ------------------------------------------------------------------------------------
    // Getters / Setters
    // ------------------------------------------------------------------------------------

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(BigDecimal percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDateTime getValidade() {
        return validade;
    }

    public void setValidade(LocalDateTime validade) {
        this.validade = validade;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    // ------------------------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemVendaDTO that))
            return false;
        return Objects.equals(produtoId, that.produtoId) && Objects.equals(quantidade, that.quantidade)
                && Objects.equals(precoUnitario, that.precoUnitario)
                && Objects.equals(percentualDesconto, that.percentualDesconto)
                && Objects.equals(valorDesconto, that.valorDesconto) && Objects.equals(total, that.total)
                && Objects.equals(lote, that.lote) && Objects.equals(validade, that.validade)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, quantidade, precoUnitario, percentualDesconto, valorDesconto, total, lote,
                validade, tenantId, traceId);
    }

    @Override
    public String toString() {
        return "ItemVendaDTO{" + "produtoId=" + produtoId + ", quantidade=" + quantidade + ", precoUnitario="
                + precoUnitario + ", percentualDesconto=" + percentualDesconto + ", valorDesconto=" + valorDesconto
                + ", total=" + total + ", lote='" + lote + '\'' + ", validade=" + validade + ", tenantId='" + tenantId
                + '\'' + ", traceId=" + traceId + '}';
    }
}
