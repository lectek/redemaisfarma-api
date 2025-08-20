package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * DTO de resposta com todos os dados detalhados de um pedido.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PedidoResponseDTO", description = "Dados completos de um pedido realizado")
public class PedidoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do pedido", example = "123")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Data e hora da criação do pedido", example = "2025-07-05T10:15:30")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataCriacao")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data e hora da entrega (se aplicável)", example = "2025-07-06T15:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataEntrega")
    private LocalDateTime dataEntrega;

    @Schema(description = "Status atual do pedido", example = "EM_PREPARO")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Valor total do pedido", example = "249.90")
    @JsonProperty("total")
    private BigDecimal total;

    @Schema(description = "Observações adicionais do pedido", example = "Cliente pediu sem sacola.")
    @JsonProperty("observacao")
    private String observacao;

    @Schema(description = "Cliente que realizou o pedido")
    @JsonProperty("cliente")
    private ClienteDTO cliente;

    @Schema(description = "Atendente responsável pelo pedido")
    @JsonProperty("atendente")
    private AtendenteDTO atendente;

    @Schema(description = "Lista de itens do pedido")
    @JsonProperty("itens")
    private List<ItemPedidoResponseDTO> itens;

    // ===========================
    // Getters e Setters
    // ===========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public AtendenteDTO getAtendente() {
        return atendente;
    }

    public void setAtendente(AtendenteDTO atendente) {
        this.atendente = atendente;
    }

    public List<ItemPedidoResponseDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoResponseDTO> itens) {
        this.itens = itens;
    }

    // ===========================
    // equals e hashCode
    // ===========================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PedidoResponseDTO))
            return false;
        PedidoResponseDTO that = (PedidoResponseDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(dataCriacao, that.dataCriacao)
                && Objects.equals(dataEntrega, that.dataEntrega) && Objects.equals(status, that.status)
                && Objects.equals(total, that.total) && Objects.equals(observacao, that.observacao)
                && Objects.equals(cliente, that.cliente) && Objects.equals(atendente, that.atendente)
                && Objects.equals(itens, that.itens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataCriacao, dataEntrega, status, total, observacao, cliente, atendente, itens);
    }

    @Override
    public String toString() {
        return "PedidoResponseDTO{" + "id=" + id + ", dataCriacao=" + dataCriacao + ", dataEntrega=" + dataEntrega
                + ", status='" + status + '\'' + ", total=" + total + ", observacao='" + observacao + '\''
                + ", cliente=" + cliente + ", atendente=" + atendente + ", itens=" + itens + '}';
    }
}
