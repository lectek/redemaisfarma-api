package br.com.redemaisfarma.application.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import br.com.redemaisfarma.domain.enums.StatusPedido;

/**
 * DTO de requisição para atualização de um pedido na API RedeMaisFarma. Permite alterar status, itens, observações e
 * vínculo com cliente.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados para atualizar um pedido já existente na farmácia")
public class AtualizarItemPedidoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{pedido.id.notNull}")
    @Schema(description = "ID do pedido a ser atualizado", example = "123")
    @JsonProperty("pedidoId")
    private Long pedidoId;

    @NotEmpty(message = "{pedido.itens.notEmpty}")
    @Schema(description = "Lista de itens do pedido")
    @JsonProperty("itens")
    private List<@Valid ItemPedidoRequestDTO> itens;

    @DecimalMin(value = "0.00", inclusive = true, message = "{pedido.total.min}")
    @Schema(description = "Valor total atualizado (se omitido, o servidor recalcula)", example = "249.90")
    @JsonProperty("total")
    private BigDecimal total;

    @Size(max = 255, message = "{pedido.observacao.size}")
    @Schema(description = "Observações ou anotações do atendente", example = "Cliente pediu sem sacola.")
    @JsonProperty("observacao")
    private String observacao;

    @NotNull(message = "{pedido.clienteId.notNull}")
    @Schema(description = "ID do cliente vinculado", example = "45")
    @JsonProperty("clienteId")
    private Long clienteId;

    @FutureOrPresent(message = "{pedido.dataEntrega.futureOrPresent}")
    @Schema(description = "Data/hora prevista para entrega", example = "2025-07-05T15:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataEntrega")
    private LocalDateTime dataEntrega;

    @NotNull(message = "{pedido.status.notNull}")
    @Schema(description = "Status atual do pedido", example = "EM_PREPARO")
    @JsonProperty("status")
    private StatusPedido status;

    // Opcional: multitenancy/rastreamento/concorrência
    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Versão para controle otimista de concorrência", example = "5")
    @JsonProperty("version")
    private Long version;

    // ===== Validações de coerência (opcional, mas recomendado) =====

    @AssertTrue(message = "{pedido.total.coerente}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isTotalCoerente() {
        if (itens == null || itens.isEmpty())
            return false;
        if (total == null)
            return true; // servidor pode recalcular se não vier
        BigDecimal soma = itens.stream().filter(Objects::nonNull).map(i -> {
            BigDecimal unit = i.getPrecoUnitario() != null ? i.getPrecoUnitario() : BigDecimal.ZERO;
            int qtd = i.getQuantidade() != null ? i.getQuantidade() : 0;
            return unit.multiply(BigDecimal.valueOf(qtd));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.setScale(2, RoundingMode.HALF_UP).compareTo(total.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    // ============================
    // Getters e Setters
    // ============================

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public List<ItemPedidoRequestDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequestDTO> itens) {
        this.itens = itens;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // ============================
    // equals, hashCode, toString
    // ============================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AtualizarItemPedidoDTO))
            return false;
        AtualizarItemPedidoDTO that = (AtualizarItemPedidoDTO) o;
        return Objects.equals(pedidoId, that.pedidoId) && Objects.equals(itens, that.itens)
                && Objects.equals(total, that.total) && Objects.equals(observacao, that.observacao)
                && Objects.equals(clienteId, that.clienteId) && Objects.equals(dataEntrega, that.dataEntrega)
                && status == that.status && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(traceId, that.traceId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedidoId, itens, total, observacao, clienteId, dataEntrega, status, tenantId, traceId,
                version);
    }

    @Override
    public String toString() {
        return "AtualizarItemPedidoDTO{" + "pedidoId=" + pedidoId + ", itens=" + itens + ", total=" + total
                + ", observacao='" + observacao + '\'' + ", clienteId=" + clienteId + ", dataEntrega=" + dataEntrega
                + ", status=" + status + ", tenantId='" + tenantId + '\'' + ", traceId=" + traceId + ", version="
                + version + '}';
    }
}
