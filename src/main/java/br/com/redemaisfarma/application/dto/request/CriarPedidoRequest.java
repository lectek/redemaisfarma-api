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
 *  jakarta.validation.constraints.Digits
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotEmpty
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import br.com.redemaisfarma.application.dto.request.FormaPagamentoRequestDTO;
import br.com.redemaisfarma.application.dto.request.ItemPedidoRequestDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(name="CriarPedidoRequest", description="Dados para cria\u00e7\u00e3o de um pedido na RedeMaisFarma")
public class CriarPedidoRequest
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico do pedido (idempot\u00eancia)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="pedidoId", access=JsonProperty.Access.READ_ONLY)
    private UUID pedidoId;
    @Schema(description="ID do cliente que realizou o pedido", example="123", required=true)
    @NotNull(message="{pedido.clienteId.notNull}")
    @Min(value=1L, message="{pedido.clienteId.min}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{pedido.clienteId.notNull}") @Min(value=1L, message="{pedido.clienteId.min}") Long clienteId;
    @Schema(description="Itens do pedido", required=true)
    @NotEmpty(message="{pedido.itens.notEmpty}")
    @Size(max=200, message="{pedido.itens.size}")
    @Valid
    @JsonProperty(value="itens")
    private @NotEmpty(message="{pedido.itens.notEmpty}") @Size(max=200, message="{pedido.itens.size}") @Valid List<@NotNull @Valid ItemPedidoRequestDTO> itens;
    @Schema(description="Endere\u00e7o de entrega completo", example="Rua das Ac\u00e1cias, 45, Centro, Jo\u00e3o Pessoa - PB", required=true)
    @NotBlank(message="{pedido.enderecoEntrega.notBlank}")
    @Size(min=5, max=200, message="{pedido.enderecoEntrega.size}")
    @JsonProperty(value="enderecoEntrega")
    private @NotBlank(message="{pedido.enderecoEntrega.notBlank}") @Size(min=5, max=200, message="{pedido.enderecoEntrega.size}") String enderecoEntrega;
    @Schema(description="Valor total do pedido", example="150.75", required=true)
    @NotNull(message="{pedido.valorTotal.notNull}")
    @DecimalMin(value="0.01", inclusive=true, message="{pedido.valorTotal.min}")
    @Digits(integer=12, fraction=2, message="{pedido.valorTotal.digits}")
    @JsonProperty(value="valorTotal")
    private @NotNull(message="{pedido.valorTotal.notNull}") @DecimalMin(value="0.01", inclusive=true, message="{pedido.valorTotal.min}") @Digits(integer=12, fraction=2, message="{pedido.valorTotal.digits}") BigDecimal valorTotal;
    @Schema(description="Forma de pagamento", required=true)
    @NotNull(message="{pedido.formaPagamento.notNull}")
    @Valid
    @JsonProperty(value="formaPagamento")
    private @NotNull(message="{pedido.formaPagamento.notNull}") @Valid FormaPagamentoRequestDTO formaPagamento;
    @Schema(description="ID do tenant", example="redemaisfarma-001", required=true)
    @NotBlank(message="{pedido.tenantId.notBlank}")
    @Size(max=100, message="{pedido.tenantId.size}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{pedido.tenantId.notBlank}") @Size(max=100, message="{pedido.tenantId.size}") String tenantId;
    @Schema(description="Token de rastreamento do pedido", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId", access=JsonProperty.Access.READ_ONLY)
    private UUID traceId;
    @Schema(description="Timestamp de cria\u00e7\u00e3o do pedido", type="string", format="date-time", example="2025-07-04T10:15:30", required=true)
    @NotNull(message="{pedido.dataCriacao.notNull}")
    @PastOrPresent(message="{pedido.dataCriacao.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataCriacao")
    private @NotNull(message="{pedido.dataCriacao.notNull}") @PastOrPresent(message="{pedido.dataCriacao.pastOrPresent}") LocalDateTime dataCriacao;
    @Schema(description="Observa\u00e7\u00f5es adicionais", example="Entregar com cuidado, atr\u00e1s do port\u00e3o branco")
    @Size(max=500, message="{pedido.observacao.size}")
    @JsonProperty(value="observacao")
    private @Size(max=500, message="{pedido.observacao.size}") String observacao;
    @Schema(description="Timestamp da \u00faltima atualiza\u00e7\u00e3o do pedido", type="string", format="date-time", example="2025-07-04T11:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataAtualizacao", access=JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataAtualizacao;

    public CriarPedidoRequest() {
    }

    public CriarPedidoRequest(UUID pedidoId, Long clienteId, List<ItemPedidoRequestDTO> itens, String enderecoEntrega, BigDecimal valorTotal, FormaPagamentoRequestDTO formaPagamento, String tenantId, UUID traceId, LocalDateTime dataCriacao, String observacao, LocalDateTime dataAtualizacao) {
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.itens = itens;
        this.enderecoEntrega = enderecoEntrega;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.dataCriacao = dataCriacao;
        this.observacao = observacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    @AssertTrue(message="{pedido.totais.coerencia}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{pedido.totais.coerencia}") boolean isTotaisCoerentes() {
        if (this.itens == null || this.itens.isEmpty() || this.valorTotal == null) {
            return false;
        }
        BigDecimal soma = this.itens.stream().map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade().intValue()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.compareTo(this.valorTotal) == 0;
    }

    @AssertTrue(message="{pedido.formaPagamento.igualTotal}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{pedido.formaPagamento.igualTotal}") boolean isFormaPagamentoIgualTotal() {
        if (this.formaPagamento == null || this.valorTotal == null || this.formaPagamento.getValor() == null) {
            return false;
        }
        return this.valorTotal.compareTo(this.formaPagamento.getValor()) == 0;
    }

    @AssertTrue(message="{pedido.datas.ordemValida}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{pedido.datas.ordemValida}") boolean isOrdemTemporalValida() {
        if (this.dataCriacao == null) {
            return false;
        }
        if (this.dataAtualizacao == null) {
            return true;
        }
        return !this.dataAtualizacao.isBefore(this.dataCriacao);
    }

    public UUID getPedidoId() {
        return this.pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemPedidoRequestDTO> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemPedidoRequestDTO> itens) {
        this.itens = itens;
    }

    public String getEnderecoEntrega() {
        return this.enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public BigDecimal getValorTotal() {
        return this.valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public FormaPagamentoRequestDTO getFormaPagamento() {
        return this.formaPagamento;
    }

    public void setFormaPagamento(FormaPagamentoRequestDTO formaPagamento) {
        this.formaPagamento = formaPagamento;
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

    public LocalDateTime getDataCriacao() {
        return this.dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return this.dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CriarPedidoRequest)) {
            return false;
        }
        CriarPedidoRequest that = (CriarPedidoRequest)o;
        return Objects.equals(this.pedidoId, that.pedidoId) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.itens, that.itens) && Objects.equals(this.enderecoEntrega, that.enderecoEntrega) && Objects.equals(this.valorTotal, that.valorTotal) && Objects.equals(this.formaPagamento, that.formaPagamento) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.dataCriacao, that.dataCriacao) && Objects.equals(this.observacao, that.observacao) && Objects.equals(this.dataAtualizacao, that.dataAtualizacao);
    }

    public int hashCode() {
        return Objects.hash(this.pedidoId, this.clienteId, this.itens, this.enderecoEntrega, this.valorTotal, this.formaPagamento, this.tenantId, this.traceId, this.dataCriacao, this.observacao, this.dataAtualizacao);
    }

    public String toString() {
        return "CriarPedidoRequest{pedidoId=" + String.valueOf(this.pedidoId) + ", clienteId=" + this.clienteId + ", itens=" + String.valueOf(this.itens) + ", enderecoEntrega='" + this.enderecoEntrega + "', valorTotal=" + String.valueOf(this.valorTotal) + ", formaPagamento=" + String.valueOf(this.formaPagamento) + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", dataCriacao=" + String.valueOf(this.dataCriacao) + ", observacao='" + this.observacao + "', dataAtualizacao=" + String.valueOf(this.dataAtualizacao) + "}";
    }
}

