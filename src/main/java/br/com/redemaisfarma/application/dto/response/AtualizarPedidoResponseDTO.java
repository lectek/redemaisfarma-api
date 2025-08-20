package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import br.com.redemaisfarma.domain.enums.StatusPedido;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de resposta após atualização de pedido. Fornece estado atual, valores, itens, entrega e metadados (tenant/trace).
 */
@Schema(name = "AtualizarPedidoResponseDTO", description = "Dados de resposta após atualização de pedido")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AtualizarPedidoResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único do pedido", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("pedidoId")
    private UUID pedidoId;

    @Schema(description = "Status do pedido", example = "CONFIRMADO")
    @JsonProperty("status")
    private StatusPedido status;

    @Schema(description = "Motivo da atualização de status", example = "Pagamento confirmado pelo gateway")
    @JsonProperty("motivo")
    private String motivo;

    @Schema(description = "Timestamp da atualização", type = "string", format = "date-time", example = "2025-07-04T12:34:56")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataAtualizacao")
    private LocalDateTime dataAtualizacao;

    @Schema(description = "Valor total atualizado do pedido", example = "250.75")
    @JsonProperty("valorTotal")
    private BigDecimal valorTotal;

    @Schema(description = "Lista de itens do pedido atualizado")
    @JsonProperty("itens")
    private List<ItemAtualizadoDTO> itens;

    @Schema(description = "ID do cliente", example = "123")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Detalhes de entrega atualizados")
    @JsonProperty("entrega")
    private InfoEntregaDTO entrega;

    // Construtores
    public AtualizarPedidoResponseDTO() {
    }

    public AtualizarPedidoResponseDTO(UUID pedidoId, StatusPedido status, String motivo, LocalDateTime dataAtualizacao,
            BigDecimal valorTotal, List<ItemAtualizadoDTO> itens, Long clienteId, String tenantId, UUID traceId,
            InfoEntregaDTO entrega) {
        this.pedidoId = pedidoId;
        this.status = status;
        this.motivo = motivo;
        this.dataAtualizacao = dataAtualizacao;
        this.valorTotal = valorTotal;
        this.itens = itens;
        this.clienteId = clienteId;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.entrega = entrega;
    }

    // Getters/Setters
    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemAtualizadoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemAtualizadoDTO> itens) {
        this.itens = itens;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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

    public InfoEntregaDTO getEntrega() {
        return entrega;
    }

    public void setEntrega(InfoEntregaDTO entrega) {
        this.entrega = entrega;
    }

    // equals / hashCode / toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AtualizarPedidoResponseDTO))
            return false;
        AtualizarPedidoResponseDTO that = (AtualizarPedidoResponseDTO) o;
        return Objects.equals(pedidoId, that.pedidoId) && status == that.status && Objects.equals(motivo, that.motivo)
                && Objects.equals(dataAtualizacao, that.dataAtualizacao) && Objects.equals(valorTotal, that.valorTotal)
                && Objects.equals(itens, that.itens) && Objects.equals(clienteId, that.clienteId)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(entrega, that.entrega);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedidoId, status, motivo, dataAtualizacao, valorTotal, itens, clienteId, tenantId, traceId,
                entrega);
    }

    @Override
    public String toString() {
        return "AtualizarPedidoResponseDTO{" + "pedidoId=" + pedidoId + ", status=" + status + ", motivo='" + motivo
                + '\'' + ", dataAtualizacao=" + dataAtualizacao + ", valorTotal=" + valorTotal + ", itens=" + itens
                + ", clienteId=" + clienteId + ", tenantId='" + tenantId + '\'' + ", traceId=" + traceId + ", entrega="
                + entrega + '}';
    }

    // —— Nested DTOs (somente para resposta; sem validação) ——

    @Schema(name = "ItemAtualizadoDTO", description = "Detalhes do item após atualização")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ItemAtualizadoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID do produto no item", example = "789")
        @JsonProperty("produtoId")
        private Long produtoId;

        @Schema(description = "Quantidade atualizada do item", example = "3")
        @JsonProperty("quantidade")
        private Integer quantidade;

        @Schema(description = "Preço unitário atualizado", example = "50.25")
        @JsonProperty("precoUnitario")
        private BigDecimal precoUnitario;

        @Schema(description = "Total do item (quantidade * preco)", example = "150.75")
        @JsonProperty("total")
        private BigDecimal total;

        public ItemAtualizadoDTO() {
        }

        public ItemAtualizadoDTO(Long produtoId, Integer quantidade, BigDecimal precoUnitario, BigDecimal total) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
            this.total = total;
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

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ItemAtualizadoDTO))
                return false;
            ItemAtualizadoDTO that = (ItemAtualizadoDTO) o;
            return Objects.equals(produtoId, that.produtoId) && Objects.equals(quantidade, that.quantidade)
                    && Objects.equals(precoUnitario, that.precoUnitario) && Objects.equals(total, that.total);
        }

        @Override
        public int hashCode() {
            return Objects.hash(produtoId, quantidade, precoUnitario, total);
        }

        @Override
        public String toString() {
            return "ItemAtualizadoDTO{" + "produtoId=" + produtoId + ", quantidade=" + quantidade + ", precoUnitario="
                    + precoUnitario + ", total=" + total + '}';
        }
    }

    @Schema(name = "InfoEntregaDTO", description = "Detalhes da entrega atualizada")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InfoEntregaDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Endereço de entrega completo", example = "Rua das Flores, 123, Ap 45")
        @JsonProperty("endereco")
        private String endereco;

        @Schema(description = "Data prevista de entrega", type = "string", format = "date-time", example = "2025-07-06T14:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("dataPrevista")
        private LocalDateTime dataPrevista;

        public InfoEntregaDTO() {
        }

        public InfoEntregaDTO(String endereco, LocalDateTime dataPrevista) {
            this.endereco = endereco;
            this.dataPrevista = dataPrevista;
        }

        public String getEndereco() {
            return endereco;
        }

        public void setEndereco(String endereco) {
            this.endereco = endereco;
        }

        public LocalDateTime getDataPrevista() {
            return dataPrevista;
        }

        public void setDataPrevista(LocalDateTime dataPrevista) {
            this.dataPrevista = dataPrevista;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof InfoEntregaDTO))
                return false;
            InfoEntregaDTO that = (InfoEntregaDTO) o;
            return Objects.equals(endereco, that.endereco) && Objects.equals(dataPrevista, that.dataPrevista);
        }

        @Override
        public int hashCode() {
            return Objects.hash(endereco, dataPrevista);
        }

        @Override
        public String toString() {
            return "InfoEntregaDTO{" + "endereco='" + endereco + '\'' + ", dataPrevista=" + dataPrevista + '}';
        }
    }
}
