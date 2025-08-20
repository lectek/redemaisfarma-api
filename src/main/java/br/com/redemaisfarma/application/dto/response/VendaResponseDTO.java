package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para detalhes de venda na API RedeMaisFarma.
 *
 * Contém informações completas da venda, incluindo dados do cliente, itens, valores, status, entrega e metadados de
 * auditoria/rastreamento.
 */
@Schema(name = "VendaResponseDTO", description = "Detalhes de uma venda registrada")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendaResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Identificação e auditoria
    // ----------------------------------------------------------------------

    @Schema(description = "ID da venda", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    @NotNull(message = "{venda.id.notNull}")
    @JsonProperty("idVenda")
    private UUID idVenda;

    @Schema(description = "Data/hora da venda", type = "string", format = "date-time", example = "2025-07-04T14:20:00", required = true)
    @NotNull(message = "{venda.dataHora.notNull}")
    @PastOrPresent(message = "{venda.dataHora.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataHora")
    private LocalDateTime dataHora;

    @Schema(description = "Token de rastreamento (UUID)")
    @JsonProperty("traceId")
    private UUID traceId;

    // ----------------------------------------------------------------------
    // Cliente e itens
    // ----------------------------------------------------------------------

    @Schema(description = "Informações do cliente", required = true)
    @NotNull(message = "{venda.cliente.notNull}")
    @Valid
    @JsonProperty("cliente")
    private ClienteResponseDTO cliente;

    @Schema(description = "Itens da venda", required = true)
    @NotEmpty(message = "{venda.itens.notEmpty}")
    @Valid
    @JsonProperty("itens")
    @JsonAlias({ "itensDetalhados" }) // mantém compatibilidade com payloads antigos
    private List<@Valid ItemVendaDTO> itens;

    // ----------------------------------------------------------------------
    // Totais e pagamento
    // ----------------------------------------------------------------------

    @Schema(description = "Quantidade total de produtos vendidos", example = "5", required = true)
    @NotNull(message = "{venda.qtdTotalProdutos.notNull}")
    @Min(value = 1, message = "{venda.qtdTotalProdutos.min}")
    @JsonProperty("qtdTotalProdutos")
    private Integer qtdTotalProdutos;

    @Schema(description = "Valor total bruto da venda", example = "100.00", required = true)
    @NotNull(message = "{venda.total.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{venda.total.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.total.digits}")
    @JsonProperty("total")
    private BigDecimal total;

    @Schema(description = "Desconto aplicado", example = "5.00")
    @PositiveOrZero(message = "{venda.descontoAplicado.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.descontoAplicado.digits}")
    @JsonProperty("descontoAplicado")
    private BigDecimal descontoAplicado;

    @Schema(description = "Valor de frete", example = "10.00")
    @PositiveOrZero(message = "{venda.frete.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.frete.digits}")
    @JsonProperty("frete")
    private BigDecimal frete;

    @Schema(description = "Valor líquido da venda (total - desconto + frete)", example = "105.00", required = true)
    @NotNull(message = "{venda.valorLiquido.notNull}")
    @DecimalMin(value = "0.00", inclusive = true, message = "{venda.valorLiquido.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.valorLiquido.digits}")
    @JsonProperty("valorLiquido")
    private BigDecimal valorLiquido;

    @Schema(description = "Forma de pagamento", example = "CREDIT_CARD", required = true)
    @NotBlank(message = "{venda.formaPagamento.notBlank}")
    @Size(max = 50, message = "{venda.formaPagamento.size}")
    @JsonProperty("formaPagamento")
    private String formaPagamento;

    @Schema(description = "QR code para pagamento", example = "https://.../qrcode.png")
    @Size(max = 500, message = "{venda.qrCodePagamento.size}")
    @JsonProperty("qrCodePagamento")
    private String qrCodePagamento;

    // ----------------------------------------------------------------------
    // Status, origem e entrega
    // ----------------------------------------------------------------------

    @Schema(description = "Status da venda", example = "CONFIRMADA", required = true, allowableValues = { "PENDENTE",
            "CONFIRMADA", "CANCELADA", "ESTORNADA" })
    @NotNull(message = "{venda.status.notNull}")
    @JsonProperty("status")
    private StatusVenda status;

    @Schema(description = "Canal de origem do pedido", example = "APP", required = true, allowableValues = { "SITE",
            "BALCAO", "DELIVERY", "APP" })
    @NotNull(message = "{venda.origemPedido.notNull}")
    @JsonProperty("origemPedido")
    private OrigemPedido origemPedido;

    @Schema(description = "Responsável pelo atendimento", example = "atendente123")
    @Size(max = 100, message = "{venda.responsavelAtendimento.size}")
    @JsonProperty("responsavelAtendimento")
    private String responsavelAtendimento;

    @Schema(description = "Entregador da venda", example = "entregador456")
    @Size(max = 100, message = "{venda.entregador.size}")
    @JsonProperty("entregador")
    private String entregador;

    @Schema(description = "Modo de entrega", example = "ENTREGA", required = true, allowableValues = { "RETIRADA",
            "ENTREGA" })
    @NotNull(message = "{venda.modoEntrega.notNull}")
    @JsonProperty("modoEntrega")
    private ModoEntrega modoEntrega;

    @Schema(description = "Código de rastreamento", example = "TRACK123456")
    @Size(max = 100, message = "{venda.codigoRastreamento.size}")
    @JsonProperty("codigoRastreamento")
    private String codigoRastreamento;

    @Schema(description = "Data/hora estimada de entrega", type = "string", format = "date-time", example = "2025-07-05T15:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataEntregaEstimada")
    private LocalDateTime dataEntregaEstimada;

    @Schema(description = "Canal de atendimento", example = "APP", allowableValues = { "SITE", "BALCAO", "DELIVERY",
            "APP" })
    @JsonProperty("canalAtendimento")
    private CanalAtendimento canalAtendimento;

    @Schema(description = "Mensagem do cliente", example = "Por favor, deixe na portaria.")
    @Size(max = 500, message = "{venda.mensagemCliente.size}")
    @JsonProperty("mensagemCliente")
    private String mensagemCliente;

    @Schema(description = "Mensagem interna", example = "Verificar cupom fidelidade.")
    @Size(max = 500, message = "{venda.mensagemInterna.size}")
    @JsonProperty("mensagemInterna")
    private String mensagemInterna;

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------

    public VendaResponseDTO() {
    }

    public VendaResponseDTO(UUID idVenda, LocalDateTime dataHora, UUID traceId, ClienteResponseDTO cliente,
            List<ItemVendaDTO> itens, Integer qtdTotalProdutos, BigDecimal total, BigDecimal descontoAplicado,
            BigDecimal frete, BigDecimal valorLiquido, String formaPagamento, String qrCodePagamento,
            StatusVenda status, OrigemPedido origemPedido, String responsavelAtendimento, String entregador,
            ModoEntrega modoEntrega, String codigoRastreamento, LocalDateTime dataEntregaEstimada,
            CanalAtendimento canalAtendimento, String mensagemCliente, String mensagemInterna) {
        this.idVenda = idVenda;
        this.dataHora = dataHora;
        this.traceId = traceId;
        this.cliente = cliente;
        this.itens = itens;
        this.qtdTotalProdutos = qtdTotalProdutos;
        this.total = total;
        this.descontoAplicado = descontoAplicado;
        this.frete = frete;
        this.valorLiquido = valorLiquido;
        this.formaPagamento = formaPagamento;
        this.qrCodePagamento = qrCodePagamento;
        this.status = status;
        this.origemPedido = origemPedido;
        this.responsavelAtendimento = responsavelAtendimento;
        this.entregador = entregador;
        this.modoEntrega = modoEntrega;
        this.codigoRastreamento = codigoRastreamento;
        this.dataEntregaEstimada = dataEntregaEstimada;
        this.canalAtendimento = canalAtendimento;
        this.mensagemCliente = mensagemCliente;
        this.mensagemInterna = mensagemInterna;
    }

    // ----------------------------------------------------------------------
    // Getters e Setters
    // ----------------------------------------------------------------------

    public UUID getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(UUID idVenda) {
        this.idVenda = idVenda;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public ClienteResponseDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteResponseDTO cliente) {
        this.cliente = cliente;
    }

    public List<ItemVendaDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemVendaDTO> itens) {
        this.itens = itens;
    }

    public Integer getQtdTotalProdutos() {
        return qtdTotalProdutos;
    }

    public void setQtdTotalProdutos(Integer qtdTotalProdutos) {
        this.qtdTotalProdutos = qtdTotalProdutos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getDescontoAplicado() {
        return descontoAplicado;
    }

    public void setDescontoAplicado(BigDecimal descontoAplicado) {
        this.descontoAplicado = descontoAplicado;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public void setFrete(BigDecimal frete) {
        this.frete = frete;
    }

    public BigDecimal getValorLiquido() {
        return valorLiquido;
    }

    public void setValorLiquido(BigDecimal valorLiquido) {
        this.valorLiquido = valorLiquido;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getQrCodePagamento() {
        return qrCodePagamento;
    }

    public void setQrCodePagamento(String qrCodePagamento) {
        this.qrCodePagamento = qrCodePagamento;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public void setStatus(StatusVenda status) {
        this.status = status;
    }

    public OrigemPedido getOrigemPedido() {
        return origemPedido;
    }

    public void setOrigemPedido(OrigemPedido origemPedido) {
        this.origemPedido = origemPedido;
    }

    public String getResponsavelAtendimento() {
        return responsavelAtendimento;
    }

    public void setResponsavelAtendimento(String responsavelAtendimento) {
        this.responsavelAtendimento = responsavelAtendimento;
    }

    public String getEntregador() {
        return entregador;
    }

    public void setEntregador(String entregador) {
        this.entregador = entregador;
    }

    public ModoEntrega getModoEntrega() {
        return modoEntrega;
    }

    public void setModoEntrega(ModoEntrega modoEntrega) {
        this.modoEntrega = modoEntrega;
    }

    public String getCodigoRastreamento() {
        return codigoRastreamento;
    }

    public void setCodigoRastreamento(String codigoRastreamento) {
        this.codigoRastreamento = codigoRastreamento;
    }

    public LocalDateTime getDataEntregaEstimada() {
        return dataEntregaEstimada;
    }

    public void setDataEntregaEstimada(LocalDateTime dataEntregaEstimada) {
        this.dataEntregaEstimada = dataEntregaEstimada;
    }

    public CanalAtendimento getCanalAtendimento() {
        return canalAtendimento;
    }

    public void setCanalAtendimento(CanalAtendimento canalAtendimento) {
        this.canalAtendimento = canalAtendimento;
    }

    public String getMensagemCliente() {
        return mensagemCliente;
    }

    public void setMensagemCliente(String mensagemCliente) {
        this.mensagemCliente = mensagemCliente;
    }

    public String getMensagemInterna() {
        return mensagemInterna;
    }

    public void setMensagemInterna(String mensagemInterna) {
        this.mensagemInterna = mensagemInterna;
    }

    // ----------------------------------------------------------------------
    // equals/hashCode/toString
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VendaResponseDTO))
            return false;
        VendaResponseDTO that = (VendaResponseDTO) o;
        return Objects.equals(idVenda, that.idVenda) && Objects.equals(dataHora, that.dataHora)
                && Objects.equals(traceId, that.traceId) && Objects.equals(cliente, that.cliente)
                && Objects.equals(itens, that.itens) && Objects.equals(qtdTotalProdutos, that.qtdTotalProdutos)
                && Objects.equals(total, that.total) && Objects.equals(descontoAplicado, that.descontoAplicado)
                && Objects.equals(frete, that.frete) && Objects.equals(valorLiquido, that.valorLiquido)
                && Objects.equals(formaPagamento, that.formaPagamento)
                && Objects.equals(qrCodePagamento, that.qrCodePagamento) && status == that.status
                && origemPedido == that.origemPedido
                && Objects.equals(responsavelAtendimento, that.responsavelAtendimento)
                && Objects.equals(entregador, that.entregador) && modoEntrega == that.modoEntrega
                && Objects.equals(codigoRastreamento, that.codigoRastreamento)
                && Objects.equals(dataEntregaEstimada, that.dataEntregaEstimada)
                && canalAtendimento == that.canalAtendimento && Objects.equals(mensagemCliente, that.mensagemCliente)
                && Objects.equals(mensagemInterna, that.mensagemInterna);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVenda, dataHora, traceId, cliente, itens, qtdTotalProdutos, total, descontoAplicado,
                frete, valorLiquido, formaPagamento, qrCodePagamento, status, origemPedido, responsavelAtendimento,
                entregador, modoEntrega, codigoRastreamento, dataEntregaEstimada, canalAtendimento, mensagemCliente,
                mensagemInterna);
    }

    @Override
    public String toString() {
        return "VendaResponseDTO{" + "idVenda=" + idVenda + ", dataHora=" + dataHora + ", traceId=" + traceId
                + ", cliente=" + (cliente != null ? cliente.getClienteId() : null) + ", itens="
                + (itens != null ? itens.size() : 0) + ", qtdTotalProdutos=" + qtdTotalProdutos + ", total=" + total
                + ", descontoAplicado=" + descontoAplicado + ", frete=" + frete + ", valorLiquido=" + valorLiquido
                + ", formaPagamento='" + formaPagamento + '\'' + ", qrCodePagamento='" + qrCodePagamento + '\''
                + ", status=" + status + ", origemPedido=" + origemPedido + ", responsavelAtendimento='"
                + responsavelAtendimento + '\'' + ", entregador='" + entregador + '\'' + ", modoEntrega=" + modoEntrega
                + ", codigoRastreamento='" + codigoRastreamento + '\'' + ", dataEntregaEstimada=" + dataEntregaEstimada
                + ", canalAtendimento=" + canalAtendimento + ", mensagemCliente='" + mensagemCliente + '\''
                + ", mensagemInterna='" + mensagemInterna + '\'' + '}';
    }

    // ----------------------------------------------------------------------
    // Enums
    // ----------------------------------------------------------------------

    public enum StatusVenda {
        PENDENTE, CONFIRMADA, CANCELADA, ESTORNADA
    }

    public enum OrigemPedido {
        SITE, BALCAO, DELIVERY, APP
    }

    public enum ModoEntrega {
        RETIRADA, ENTREGA
    }

    public enum CanalAtendimento {
        SITE, BALCAO, DELIVERY, APP
    }
}
