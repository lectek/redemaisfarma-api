/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.AssertTrue
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotEmpty
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import br.com.redemaisfarma.application.dto.request.FormaPagamentoRequestDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="VendaRequestDTO", description="Dados para registro de venda")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class VendaRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do cliente", example="123", required=true)
    @NotNull(message="{venda.clienteId.notNull}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{venda.clienteId.notNull}") Long clienteId;
    @Schema(description="Itens da venda", required=true)
    @NotEmpty(message="{venda.itens.notEmpty}")
    @Valid
    @JsonProperty(value="itens")
    private @NotEmpty(message="{venda.itens.notEmpty}") @Valid List<@Valid ItemVendaDTO> itens;
    @Schema(description="Valor total bruto (soma dos itens)", example="200.00", required=true)
    @NotNull(message="{venda.totalBruto.notNull}")
    @DecimalMin(value="0.00", inclusive=false, message="{venda.totalBruto.min}")
    @JsonProperty(value="totalBruto")
    private @NotNull(message="{venda.totalBruto.notNull}") @DecimalMin(value="0.00", inclusive=false, message="{venda.totalBruto.min}") BigDecimal totalBruto;
    @Schema(description="Total de descontos aplicados", example="20.00")
    @DecimalMin(value="0.00", inclusive=true, message="{venda.totalDescontos.min}")
    @JsonProperty(value="totalDescontos")
    private @DecimalMin(value="0.00", inclusive=true, message="{venda.totalDescontos.min}") BigDecimal totalDescontos;
    @Schema(description="Total l\u00edquido (bruto - descontos)", example="180.00", required=true)
    @NotNull(message="{venda.totalLiquido.notNull}")
    @DecimalMin(value="0.00", inclusive=false, message="{venda.totalLiquido.min}")
    @JsonProperty(value="totalLiquido")
    private @NotNull(message="{venda.totalLiquido.notNull}") @DecimalMin(value="0.00", inclusive=false, message="{venda.totalLiquido.min}") BigDecimal totalLiquido;
    @Schema(description="Formas de pagamento da venda", required=true)
    @NotEmpty(message="{venda.formasPagamento.notEmpty}")
    @Valid
    @JsonProperty(value="formasPagamento")
    private @NotEmpty(message="{venda.formasPagamento.notEmpty}") @Valid List<@Valid FormaPagamentoRequestDTO> formasPagamento;
    @Schema(description="Tipo da venda (PRESENCIAL ou ONLINE)", example="PRESENCIAL", required=true)
    @NotNull(message="{venda.tipoVenda.notNull}")
    @JsonProperty(value="tipoVenda")
    private @NotNull(message="{venda.tipoVenda.notNull}") TipoVenda tipoVenda;
    @Schema(description="ID do ponto de venda (obrigat\u00f3rio para PRESENCIAL)", example="10")
    @JsonProperty(value="pontoVendaId")
    private Long pontoVendaId;
    @Schema(description="Data/hora da venda", type="string", format="date-time", example="2025-07-04T10:15:30")
    @NotNull(message="{venda.dataVenda.notNull}")
    @PastOrPresent(message="{venda.dataVenda.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataVenda")
    private @NotNull(message="{venda.dataVenda.notNull}") @PastOrPresent(message="{venda.dataVenda.pastOrPresent}") LocalDateTime dataVenda;
    @Schema(description="Observa\u00e7\u00f5es adicionais", example="Entregar em domic\u00edlio na parte da tarde")
    @Size(max=500, message="{venda.observacao.size}")
    @JsonProperty(value="observacao")
    private @Size(max=500, message="{venda.observacao.size}") String observacao;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001", required=true)
    @NotBlank(message="{venda.tenantId.notBlank}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{venda.tenantId.notBlank}") String tenantId;
    @Schema(description="Token de rastreamento (UUID) para auditoria", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;

    @AssertTrue(message="{venda.totalLiquido.coerencia}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{venda.totalLiquido.coerencia}") boolean isTotaisCoerentes() {
        BigDecimal descontos;
        if (this.totalBruto == null || this.totalLiquido == null) {
            return false;
        }
        if (this.totalBruto.signum() < 0 || this.totalLiquido.signum() <= 0) {
            return false;
        }
        BigDecimal bigDecimal = descontos = this.totalDescontos != null ? this.totalDescontos : BigDecimal.ZERO;
        if (descontos.signum() < 0 || descontos.compareTo(this.totalBruto) > 0) {
            return false;
        }
        return this.totalBruto.subtract(descontos).compareTo(this.totalLiquido) == 0;
    }

    @AssertTrue(message="{venda.formasPagamento.somaIgualTotalLiquido}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{venda.formasPagamento.somaIgualTotalLiquido}") boolean isSomaFormasPagamentoValida() {
        if (this.formasPagamento == null || this.formasPagamento.isEmpty() || this.totalLiquido == null) {
            return false;
        }
        BigDecimal soma = this.formasPagamento.stream().map(FormaPagamentoRequestDTO::getValor).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.compareTo(this.totalLiquido) == 0;
    }

    @AssertTrue(message="{venda.pontoVenda.obrigatorio.presencial}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{venda.pontoVenda.obrigatorio.presencial}") boolean isPontoVendaValido() {
        if (this.tipoVenda == null) {
            return false;
        }
        if (this.tipoVenda == TipoVenda.PRESENCIAL) {
            return this.pontoVendaId != null;
        }
        return true;
    }

    @AssertTrue(message="{venda.itens.somaIgualTotalBruto}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{venda.itens.somaIgualTotalBruto}") boolean isSomaItensIgualTotalBruto() {
        if (this.itens == null || this.itens.isEmpty() || this.totalBruto == null) {
            return false;
        }
        BigDecimal soma = this.itens.stream().map(ItemVendaDTO::getSubtotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.compareTo(this.totalBruto) == 0;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemVendaDTO> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemVendaDTO> itens) {
        this.itens = itens;
    }

    public BigDecimal getTotalBruto() {
        return this.totalBruto;
    }

    public void setTotalBruto(BigDecimal totalBruto) {
        this.totalBruto = totalBruto;
    }

    public BigDecimal getTotalDescontos() {
        return this.totalDescontos;
    }

    public void setTotalDescontos(BigDecimal totalDescontos) {
        this.totalDescontos = totalDescontos;
    }

    public BigDecimal getTotalLiquido() {
        return this.totalLiquido;
    }

    public void setTotalLiquido(BigDecimal totalLiquido) {
        this.totalLiquido = totalLiquido;
    }

    public List<FormaPagamentoRequestDTO> getFormasPagamento() {
        return this.formasPagamento;
    }

    public void setFormasPagamento(List<FormaPagamentoRequestDTO> formasPagamento) {
        this.formasPagamento = formasPagamento;
    }

    public TipoVenda getTipoVenda() {
        return this.tipoVenda;
    }

    public void setTipoVenda(TipoVenda tipoVenda) {
        this.tipoVenda = tipoVenda;
    }

    public Long getPontoVendaId() {
        return this.pontoVendaId;
    }

    public void setPontoVendaId(Long pontoVendaId) {
        this.pontoVendaId = pontoVendaId;
    }

    public LocalDateTime getDataVenda() {
        return this.dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public static enum TipoVenda {
        PRESENCIAL,
        ONLINE;

    }

    @Schema(name="ItemVendaDTO", description="Item de venda")
    public static class ItemVendaDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID do produto", example="987", required=true)
        @NotNull(message="{venda.item.produtoId.notNull}")
        private @NotNull(message="{venda.item.produtoId.notNull}") Long produtoId;
        @Schema(description="Quantidade do item", example="2", required=true)
        @NotNull(message="{venda.item.quantidade.notNull}")
        @Min(value=1L, message="{venda.item.quantidade.min}")
        private @NotNull(message="{venda.item.quantidade.notNull}") @Min(value=1L, message="{venda.item.quantidade.min}") Integer quantidade;
        @Schema(description="Pre\u00e7o unit\u00e1rio", example="100.00", required=true)
        @NotNull(message="{venda.item.precoUnitario.notNull}")
        @DecimalMin(value="0.00", inclusive=false, message="{venda.item.precoUnitario.min}")
        private @NotNull(message="{venda.item.precoUnitario.notNull}") @DecimalMin(value="0.00", inclusive=false, message="{venda.item.precoUnitario.min}") BigDecimal precoUnitario;
        @Schema(description="Subtotal do item (quantidade * pre\u00e7o unit\u00e1rio)", example="200.00", required=true)
        @NotNull(message="{venda.item.subtotal.notNull}")
        @DecimalMin(value="0.00", inclusive=false, message="{venda.item.subtotal.min}")
        private @NotNull(message="{venda.item.subtotal.notNull}") @DecimalMin(value="0.00", inclusive=false, message="{venda.item.subtotal.min}") BigDecimal subtotal;

        @AssertTrue(message="{venda.item.subtotal.coerencia}")
        @JsonProperty(access=JsonProperty.Access.READ_ONLY)
        public @AssertTrue(message="{venda.item.subtotal.coerencia}") boolean isSubtotalCoerente() {
            if (this.quantidade == null || this.precoUnitario == null || this.subtotal == null) {
                return false;
            }
            BigDecimal esperado = this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade.intValue()));
            return esperado.compareTo(this.subtotal) == 0;
        }

        public Long getProdutoId() {
            return this.produtoId;
        }

        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }

        public Integer getQuantidade() {
            return this.quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }

        public BigDecimal getPrecoUnitario() {
            return this.precoUnitario;
        }

        public void setPrecoUnitario(BigDecimal precoUnitario) {
            this.precoUnitario = precoUnitario;
        }

        public BigDecimal getSubtotal() {
            return this.subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }
}

