package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(name = "ItemPedidoResponse", description = "Item pertencente a um pedido")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPedidoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do item do pedido", example = "9876")
    @JsonProperty("itemId")
    private Long itemId;

    @Schema(description = "ID do pedido", example = "1234")
    @JsonProperty("pedidoId")
    private Long pedidoId;

    @Schema(description = "ID do produto", example = "101")
    @JsonProperty("produtoId")
    private Long produtoId;

    @Schema(description = "SKU do produto", example = "PRD-001-ABC")
    @JsonProperty("sku")
    private String sku;

    @Schema(description = "Nome do produto", example = "Dipirona 500mg")
    @JsonProperty("nomeProduto")
    private String nomeProduto;

    @Schema(description = "Categoria do produto", example = "ANALGESICO")
    @JsonProperty("categoria")
    private String categoria;

    @Schema(description = "Código de barras (EAN/UPC)", example = "7891234567895")
    @JsonProperty("codigoBarras")
    private String codigoBarras;

    @Schema(description = "URL da imagem do produto", example = "https://cdn.farma/img/produto1.jpg")
    @JsonProperty("imagemUrl")
    private String imagemUrl;

    @Schema(description = "Unidade de venda", example = "UN")
    @JsonProperty("unidade")
    private String unidade;

    @Schema(description = "Quantidade do item", example = "3")
    @JsonProperty("quantidade")
    private Integer quantidade;

    @Schema(description = "Preço unitário vigente", example = "12.50")
    @JsonProperty("precoUnitario")
    private BigDecimal precoUnitario;

    @Schema(description = "Percentual de desconto aplicado (0-100)", example = "5.00")
    @JsonProperty("percentualDesconto")
    private BigDecimal percentualDesconto;

    @Schema(description = "Valor absoluto de desconto", example = "1.25")
    @JsonProperty("valorDesconto")
    private BigDecimal valorDesconto;

    @Schema(description = "Subtotal do item (qtd x unitário - desconto)", example = "36.25")
    @JsonProperty("total")
    private BigDecimal total;

    // -------------------------------------------------
    // Construtores
    // -------------------------------------------------
    public ItemPedidoResponse() {
    }

    public ItemPedidoResponse(Long itemId, Long pedidoId, Long produtoId, String sku, String nomeProduto,
            String categoria, String codigoBarras, String imagemUrl, String unidade, Integer quantidade,
            BigDecimal precoUnitario, BigDecimal percentualDesconto, BigDecimal valorDesconto, BigDecimal total) {
        this.itemId = itemId;
        this.pedidoId = pedidoId;
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.imagemUrl = imagemUrl;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.percentualDesconto = percentualDesconto;
        this.valorDesconto = valorDesconto;
        this.total = total;
    }

    // -------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
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

    // -------------------------------------------------
    // equals / hashCode / toString
    // -------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemPedidoResponse that))
            return false;
        return Objects.equals(itemId, that.itemId) && Objects.equals(pedidoId, that.pedidoId)
                && Objects.equals(produtoId, that.produtoId) && Objects.equals(sku, that.sku)
                && Objects.equals(nomeProduto, that.nomeProduto) && Objects.equals(categoria, that.categoria)
                && Objects.equals(codigoBarras, that.codigoBarras) && Objects.equals(imagemUrl, that.imagemUrl)
                && Objects.equals(unidade, that.unidade) && Objects.equals(quantidade, that.quantidade)
                && Objects.equals(precoUnitario, that.precoUnitario)
                && Objects.equals(percentualDesconto, that.percentualDesconto)
                && Objects.equals(valorDesconto, that.valorDesconto) && Objects.equals(total, that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, pedidoId, produtoId, sku, nomeProduto, categoria, codigoBarras, imagemUrl, unidade,
                quantidade, precoUnitario, percentualDesconto, valorDesconto, total);
    }

    @Override
    public String toString() {
        return "ItemPedidoResponse{" + "itemId=" + itemId + ", pedidoId=" + pedidoId + ", produtoId=" + produtoId
                + ", sku='" + sku + '\'' + ", nomeProduto='" + nomeProduto + '\'' + ", categoria='" + categoria + '\''
                + ", codigoBarras='" + codigoBarras + '\'' + ", imagemUrl='" + imagemUrl + '\'' + ", unidade='"
                + unidade + '\'' + ", quantidade=" + quantidade + ", precoUnitario=" + precoUnitario
                + ", percentualDesconto=" + percentualDesconto + ", valorDesconto=" + valorDesconto + ", total=" + total
                + '}';
    }
}
