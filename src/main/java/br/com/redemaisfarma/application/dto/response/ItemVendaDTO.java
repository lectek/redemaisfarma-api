package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO de resposta contendo os dados de um item da venda.
 */
@Schema(name = "ItemVendaDTO", description = "Item individual presente em uma venda")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemVendaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do produto", example = "456")
    @JsonProperty("produtoId")
    private Long produtoId;

    @Schema(description = "SKU do produto", example = "PRD-001-ABC")
    @JsonProperty("sku")
    private String sku;

    @Schema(description = "Nome do produto", example = "Dipirona 500mg")
    @JsonProperty("nomeProduto")
    private String nomeProduto;

    @Schema(description = "Quantidade vendida", example = "2")
    @JsonProperty("quantidade")
    private Integer quantidade;

    @Schema(description = "Preço unitário (sem desconto)", example = "12.50")
    @JsonProperty("precoUnitario")
    private BigDecimal precoUnitario;

    @Schema(description = "Percentual de desconto aplicado ao item (0-100)", example = "5.00")
    @JsonProperty("percentualDesconto")
    private BigDecimal percentualDesconto;

    @Schema(description = "Valor absoluto de desconto aplicado ao item", example = "0.62")
    @JsonProperty("valorDesconto")
    private BigDecimal valorDesconto;

    @Schema(description = "Subtotal do item (quantidade x unitário - desconto)", example = "23.38")
    @JsonProperty("total")
    private BigDecimal total;

    @Schema(description = "Código do lote (quando aplicável)", example = "Lote1234")
    @JsonProperty("lote")
    private String lote;

    @Schema(description = "Validade do produto (quando aplicável)", type = "string", format = "date", example = "2026-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("validade")
    private LocalDate validade;

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------

    public ItemVendaDTO() {
    }

    public ItemVendaDTO(Long produtoId, String sku, String nomeProduto, Integer quantidade, BigDecimal precoUnitario,
            BigDecimal percentualDesconto, BigDecimal valorDesconto, BigDecimal total, String lote,
            LocalDate validade) {
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.percentualDesconto = percentualDesconto;
        this.valorDesconto = valorDesconto;
        this.total = total;
        this.lote = lote;
        this.validade = validade;
    }

    // ----------------------------------------------------------------------
    // Getters e Setters
    // ----------------------------------------------------------------------

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
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

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    // ----------------------------------------------------------------------
    // Utilitários
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemVendaDTO))
            return false;
        ItemVendaDTO that = (ItemVendaDTO) o;
        return Objects.equals(produtoId, that.produtoId) && Objects.equals(sku, that.sku)
                && Objects.equals(nomeProduto, that.nomeProduto) && Objects.equals(quantidade, that.quantidade)
                && Objects.equals(precoUnitario, that.precoUnitario)
                && Objects.equals(percentualDesconto, that.percentualDesconto)
                && Objects.equals(valorDesconto, that.valorDesconto) && Objects.equals(total, that.total)
                && Objects.equals(lote, that.lote) && Objects.equals(validade, that.validade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, sku, nomeProduto, quantidade, precoUnitario, percentualDesconto, valorDesconto,
                total, lote, validade);
    }

    @Override
    public String toString() {
        return "ItemVendaDTO{" + "produtoId=" + produtoId + ", sku='" + sku + '\'' + ", nomeProduto='" + nomeProduto
                + '\'' + ", quantidade=" + quantidade + ", precoUnitario=" + precoUnitario + ", percentualDesconto="
                + percentualDesconto + ", valorDesconto=" + valorDesconto + ", total=" + total + ", lote='" + lote
                + '\'' + ", validade=" + validade + '}';
    }
}
