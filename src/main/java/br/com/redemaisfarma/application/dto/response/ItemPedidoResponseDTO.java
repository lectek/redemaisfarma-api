package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ItemPedidoResponseDTO", description = "Item que compõe o pedido")
public class ItemPedidoResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do produto", example = "1001")
    @JsonProperty("produtoId")
    private Long produtoId;

    @Schema(description = "Nome do produto", example = "Dipirona 500mg")
    @JsonProperty("nomeProduto")
    private String nomeProduto;

    @Schema(description = "Quantidade solicitada", example = "2")
    @JsonProperty("quantidade")
    private Integer quantidade;

    @Schema(description = "Preço unitário", example = "12.50")
    @JsonProperty("precoUnitario")
    private BigDecimal precoUnitario;

    @Schema(description = "Subtotal do item (quantidade x preço)", example = "25.00")
    @JsonProperty("subtotal")
    private BigDecimal subtotal;

    // Getters/Setters
    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemPedidoResponseDTO))
            return false;
        ItemPedidoResponseDTO that = (ItemPedidoResponseDTO) o;
        return Objects.equals(produtoId, that.produtoId) && Objects.equals(nomeProduto, that.nomeProduto)
                && Objects.equals(quantidade, that.quantidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, nomeProduto, quantidade);
    }
}
