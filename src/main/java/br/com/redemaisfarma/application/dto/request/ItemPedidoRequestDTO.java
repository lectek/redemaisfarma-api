package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(name = "ItemPedidoRequestDTO", description = "DTO para item do pedido no momento da criação")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPedidoRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do produto", example = "101", required = true)
    @NotNull(message = "{itemPedido.produtoId.notNull}")
    @JsonProperty("produtoId")
    private Long produtoId;

    @Schema(description = "Quantidade desejada do produto", example = "3", required = true)
    @NotNull(message = "{itemPedido.quantidade.notNull}")
    @Min(value = 1, message = "{itemPedido.quantidade.min}")
    @JsonProperty("quantidade")
    private Integer quantidade;

    @Schema(description = "Preço unitário do produto no momento do pedido", example = "9.90", required = true)
    @NotNull(message = "{itemPedido.precoUnitario.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{itemPedido.precoUnitario.min}")
    @Digits(integer = 12, fraction = 2, message = "{itemPedido.precoUnitario.digits}")
    @JsonProperty("precoUnitario")
    private BigDecimal precoUnitario;

    @Schema(description = "Subtotal calculado (quantidade x preço unitário) - calculado pelo servidor", example = "29.70", accessMode = Schema.AccessMode.READ_ONLY)
    @Digits(integer = 12, fraction = 2, message = "{itemPedido.subtotal.digits}")
    @JsonProperty(value = "subtotal", access = JsonProperty.Access.READ_ONLY)
    private BigDecimal subtotal;

    public ItemPedidoRequestDTO() {
    }

    public ItemPedidoRequestDTO(Long produtoId, Integer quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    } // servidor define

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemPedidoRequestDTO that))
            return false;
        return Objects.equals(produtoId, that.produtoId) && Objects.equals(quantidade, that.quantidade)
                && Objects.equals(precoUnitario, that.precoUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, quantidade, precoUnitario);
    }

    @Override
    public String toString() {
        return "ItemPedidoRequestDTO{" + "produtoId=" + produtoId + ", quantidade=" + quantidade + ", precoUnitario="
                + precoUnitario + '}';
    }
}
