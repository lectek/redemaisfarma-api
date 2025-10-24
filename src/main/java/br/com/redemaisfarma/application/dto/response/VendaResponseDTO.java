/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonAlias
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Digits
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotEmpty
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.PositiveOrZero
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.response;

import br.com.redemaisfarma.application.dto.response.ClienteResponseDTO;
import br.com.redemaisfarma.application.dto.response.ItemVendaDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="VendaResponseDTO", description="Detalhes de uma venda registrada")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class VendaResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID da venda", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", required=true)
    @NotNull(message="{venda.id.notNull}")
    @JsonProperty(value="idVenda")
    private @NotNull(message="{venda.id.notNull}") UUID idVenda;
    @Schema(description="Data/hora da venda", type="string", format="date-time", example="2025-07-04T14:20:00", required=true)
    @NotNull(message="{venda.dataHora.notNull}")
    @PastOrPresent(message="{venda.dataHora.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataHora")
    private @NotNull(message="{venda.dataHora.notNull}") @PastOrPresent(message="{venda.dataHora.pastOrPresent}") LocalDateTime dataHora;
    @Schema(description="Token de rastreamento (UUID)")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Informa\u00e7\u00f5es do cliente", required=true)
    @NotNull(message="{venda.cliente.notNull}")
    @Valid
    @JsonProperty(value="cliente")
    private @NotNull(message="{venda.cliente.notNull}") @Valid ClienteResponseDTO cliente;
    @Schema(description="Itens da venda", required=true)
    @NotEmpty(message="{venda.itens.notEmpty}")
    @Valid
    @JsonProperty(value="itens")
    @JsonAlias(value={"itensDetalhados"})
    private @NotEmpty(message="{venda.itens.notEmpty}") @Valid List<@Valid ItemVendaDTO> itens;
    @Schema(description="Quantidade total de produtos vendidos", example="5", required=true)
    @NotNull(message="{venda.qtdTotalProdutos.notNull}")
    @Min(value=1L, message="{venda.qtdTotalProdutos.min}")
    @JsonProperty(value="qtdTotalProdutos")
    private @NotNull(message="{venda.qtdTotalProdutos.notNull}") @Min(value=1L, message="{venda.qtdTotalProdutos.min}") Integer qtdTotalProdutos;
    @Schema(description="Valor total bruto da venda", example="100.00", required=true)
    @NotNull(message="{venda.total.notNull}")
    @DecimalMin(value="0.01", inclusive=true, message="{venda.total.min}")
    @Digits(integer=14, fraction=2, message="{venda.total.digits}")
    @JsonProperty(value="total")
    private @NotNull(message="{venda.total.notNull}") @DecimalMin(value="0.01", inclusive=true, message="{venda.total.min}") @Digits(integer=14, fraction=2, message="{venda.total.digits}") BigDecimal total;
    @Schema(description="Desconto aplicado", example="5.00")
    @PositiveOrZero(message="{venda.descontoAplicado.min}")
    @Digits(integer=14, fraction=2, message="{venda.descontoAplicado.digits}")
    @JsonProperty(value="descontoAplicado")
    private @PositiveOrZero(message="{venda.descontoAplicado.min}") @Digits(integer=14, fraction=2, message="{venda.descontoAplicado.digits}") BigDecimal descontoAplicado;
    @Schema(description="Valor de frete", example="10.00")
    @PositiveOrZero(message="{venda.frete.min}")
    @Digits(integer=14, fraction=2, message="{venda.frete.digits}")
    @JsonProperty(value="frete")
    private @PositiveOrZero(message="{venda.frete.min}") @Digits(integer=14, fraction=2, message="{venda.frete.digits}") BigDecimal frete;
    @Schema(description="Valor l\u00edquido da venda (total - desconto + frete)", example="105.00", required=true)
    @NotNull(message="{venda.valorLiquido.notNull}")
    @DecimalMin(value="0.00", inclusive=true, message="{venda.valorLiquido.min}")
    @Digits(integer=14, fraction=2, message="{venda.valorLiquido.digits}")
    @JsonProperty(value="valorLiquido")
    private @NotNull(message="{venda.valorLiquido.notNull}") @DecimalMin(value="0.00", inclusive=true, message="{venda.valorLiquido.min}") @Digits(integer=14, fraction=2, message="{venda.valorLiquido.digits}") BigDecimal valorLiquido;
    @Schema(description="Forma de pagamento", example="CREDIT_CARD", required=true)
    @NotBlank(message="{venda.formaPagamento.notBlank}")
    @Size(max=50, message="{venda.formaPagamento.size}")
    @JsonProperty(value="formaPagamento")
    private @NotBlank(message="{venda.formaPagamento.notBlank}") @Size(max=50, message="{venda.formaPagamento.size}") String formaPagamento;
    @Schema(description="QR code para pagamento", example="https://.../qrcode.png")
    @Size(max=500, message="{venda.qrCodePagamento.size}")
    @JsonProperty(value="qrCodePagamento")
    private @Size(max=500, message="{venda.qrCodePagamento.size}") String qrCodePagamento;
    @Schema(description="Status da venda", example="CONFIRMADA", required=true, allowableValues={"PENDENTE", "CONFIRMADA", "CANCELADA", "ESTORNADA"})
    @NotNull(message="{venda.status.notNull}")
    @JsonProperty(value="status")
    private @NotNull(message="{venda.status.notNull}") StatusVenda status;
    @Schema(description="Canal de origem do pedido", example="APP", required=true, allowableValues={"SITE", "BALCAO", "DELIVERY", "APP"})
    @NotNull(message="{venda.origemPedido.notNull}")
    @JsonProperty(value="origemPedido")
    private @NotNull(message="{venda.origemPedido.notNull}") OrigemPedido origemPedido;
    @Schema(description="Respons\u00e1vel pelo atendimento", example="atendente123")
    @Size(max=100, message="{venda.responsavelAtendimento.size}")
    @JsonProperty(value="responsavelAtendimento")
    private @Size(max=100, message="{venda.responsavelAtendimento.size}") String responsavelAtendimento;
    @Schema(description="Entregador da venda", example="entregador456")
    @Size(max=100, message="{venda.entregador.size}")
    @JsonProperty(value="entregador")
    private @Size(max=100, message="{venda.entregador.size}") String entregador;
    @Schema(description="Modo de entrega", example="ENTREGA", required=true, allowableValues={"RETIRADA", "ENTREGA"})
    @NotNull(message="{venda.modoEntrega.notNull}")
    @JsonProperty(value="modoEntrega")
    private @NotNull(message="{venda.modoEntrega.notNull}") ModoEntrega modoEntrega;
    @Schema(description="C\u00f3digo de rastreamento", example="TRACK123456")
    @Size(max=100, message="{venda.codigoRastreamento.size}")
    @JsonProperty(value="codigoRastreamento")
    private @Size(max=100, message="{venda.codigoRastreamento.size}") String codigoRastreamento;
    @Schema(description="Data/hora estimada de entrega", type="string", format="date-time", example="2025-07-05T15:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataEntregaEstimada")
    private LocalDateTime dataEntregaEstimada;
    @Schema(description="Canal de atendimento", example="APP", allowableValues={"SITE", "BALCAO", "DELIVERY", "APP"})
    @JsonProperty(value="canalAtendimento")
    private CanalAtendimento canalAtendimento;
    @Schema(description="Mensagem do cliente", example="Por favor, deixe na portaria.")
    @Size(max=500, message="{venda.mensagemCliente.size}")
    @JsonProperty(value="mensagemCliente")
    private @Size(max=500, message="{venda.mensagemCliente.size}") String mensagemCliente;
    @Schema(description="Mensagem interna", example="Verificar cupom fidelidade.")
    @Size(max=500, message="{venda.mensagemInterna.size}")
    @JsonProperty(value="mensagemInterna")
    private @Size(max=500, message="{venda.mensagemInterna.size}") String mensagemInterna;

    public VendaResponseDTO() {
    }

    public VendaResponseDTO(UUID idVenda, LocalDateTime dataHora, UUID traceId, ClienteResponseDTO cliente, List<ItemVendaDTO> itens, Integer qtdTotalProdutos, BigDecimal total, BigDecimal descontoAplicado, BigDecimal frete, BigDecimal valorLiquido, String formaPagamento, String qrCodePagamento, StatusVenda status, OrigemPedido origemPedido, String responsavelAtendimento, String entregador, ModoEntrega modoEntrega, String codigoRastreamento, LocalDateTime dataEntregaEstimada, CanalAtendimento canalAtendimento, String mensagemCliente, String mensagemInterna) {
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

    public UUID getIdVenda() {
        return this.idVenda;
    }

    public void setIdVenda(UUID idVenda) {
        this.idVenda = idVenda;
    }

    public LocalDateTime getDataHora() {
        return this.dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public ClienteResponseDTO getCliente() {
        return this.cliente;
    }

    public void setCliente(ClienteResponseDTO cliente) {
        this.cliente = cliente;
    }

    public List<ItemVendaDTO> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemVendaDTO> itens) {
        this.itens = itens;
    }

    public Integer getQtdTotalProdutos() {
        return this.qtdTotalProdutos;
    }

    public void setQtdTotalProdutos(Integer qtdTotalProdutos) {
        this.qtdTotalProdutos = qtdTotalProdutos;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getDescontoAplicado() {
        return this.descontoAplicado;
    }

    public void setDescontoAplicado(BigDecimal descontoAplicado) {
        this.descontoAplicado = descontoAplicado;
    }

    public BigDecimal getFrete() {
        return this.frete;
    }

    public void setFrete(BigDecimal frete) {
        this.frete = frete;
    }

    public BigDecimal getValorLiquido() {
        return this.valorLiquido;
    }

    public void setValorLiquido(BigDecimal valorLiquido) {
        this.valorLiquido = valorLiquido;
    }

    public String getFormaPagamento() {
        return this.formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getQrCodePagamento() {
        return this.qrCodePagamento;
    }

    public void setQrCodePagamento(String qrCodePagamento) {
        this.qrCodePagamento = qrCodePagamento;
    }

    public StatusVenda getStatus() {
        return this.status;
    }

    public void setStatus(StatusVenda status) {
        this.status = status;
    }

    public OrigemPedido getOrigemPedido() {
        return this.origemPedido;
    }

    public void setOrigemPedido(OrigemPedido origemPedido) {
        this.origemPedido = origemPedido;
    }

    public String getResponsavelAtendimento() {
        return this.responsavelAtendimento;
    }

    public void setResponsavelAtendimento(String responsavelAtendimento) {
        this.responsavelAtendimento = responsavelAtendimento;
    }

    public String getEntregador() {
        return this.entregador;
    }

    public void setEntregador(String entregador) {
        this.entregador = entregador;
    }

    public ModoEntrega getModoEntrega() {
        return this.modoEntrega;
    }

    public void setModoEntrega(ModoEntrega modoEntrega) {
        this.modoEntrega = modoEntrega;
    }

    public String getCodigoRastreamento() {
        return this.codigoRastreamento;
    }

    public void setCodigoRastreamento(String codigoRastreamento) {
        this.codigoRastreamento = codigoRastreamento;
    }

    public LocalDateTime getDataEntregaEstimada() {
        return this.dataEntregaEstimada;
    }

    public void setDataEntregaEstimada(LocalDateTime dataEntregaEstimada) {
        this.dataEntregaEstimada = dataEntregaEstimada;
    }

    public CanalAtendimento getCanalAtendimento() {
        return this.canalAtendimento;
    }

    public void setCanalAtendimento(CanalAtendimento canalAtendimento) {
        this.canalAtendimento = canalAtendimento;
    }

    public String getMensagemCliente() {
        return this.mensagemCliente;
    }

    public void setMensagemCliente(String mensagemCliente) {
        this.mensagemCliente = mensagemCliente;
    }

    public String getMensagemInterna() {
        return this.mensagemInterna;
    }

    public void setMensagemInterna(String mensagemInterna) {
        this.mensagemInterna = mensagemInterna;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VendaResponseDTO)) {
            return false;
        }
        VendaResponseDTO that = (VendaResponseDTO)o;
        return Objects.equals(this.idVenda, that.idVenda) && Objects.equals(this.dataHora, that.dataHora) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.cliente, that.cliente) && Objects.equals(this.itens, that.itens) && Objects.equals(this.qtdTotalProdutos, that.qtdTotalProdutos) && Objects.equals(this.total, that.total) && Objects.equals(this.descontoAplicado, that.descontoAplicado) && Objects.equals(this.frete, that.frete) && Objects.equals(this.valorLiquido, that.valorLiquido) && Objects.equals(this.formaPagamento, that.formaPagamento) && Objects.equals(this.qrCodePagamento, that.qrCodePagamento) && this.status == that.status && this.origemPedido == that.origemPedido && Objects.equals(this.responsavelAtendimento, that.responsavelAtendimento) && Objects.equals(this.entregador, that.entregador) && this.modoEntrega == that.modoEntrega && Objects.equals(this.codigoRastreamento, that.codigoRastreamento) && Objects.equals(this.dataEntregaEstimada, that.dataEntregaEstimada) && this.canalAtendimento == that.canalAtendimento && Objects.equals(this.mensagemCliente, that.mensagemCliente) && Objects.equals(this.mensagemInterna, that.mensagemInterna);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.idVenda, this.dataHora, this.traceId, this.cliente, this.itens, this.qtdTotalProdutos, this.total, this.descontoAplicado, this.frete, this.valorLiquido, this.formaPagamento, this.qrCodePagamento, this.status, this.origemPedido, this.responsavelAtendimento, this.entregador, this.modoEntrega, this.codigoRastreamento, this.dataEntregaEstimada, this.canalAtendimento, this.mensagemCliente, this.mensagemInterna});
    }

    public String toString() {
        return "VendaResponseDTO{idVenda=" + String.valueOf(this.idVenda) + ", dataHora=" + String.valueOf(this.dataHora) + ", traceId=" + String.valueOf(this.traceId) + ", cliente=" + String.valueOf(this.cliente != null ? this.cliente.getClienteId() : null) + ", itens=" + (this.itens != null ? this.itens.size() : 0) + ", qtdTotalProdutos=" + this.qtdTotalProdutos + ", total=" + String.valueOf(this.total) + ", descontoAplicado=" + String.valueOf(this.descontoAplicado) + ", frete=" + String.valueOf(this.frete) + ", valorLiquido=" + String.valueOf(this.valorLiquido) + ", formaPagamento='" + this.formaPagamento + "', qrCodePagamento='" + this.qrCodePagamento + "', status=" + String.valueOf((Object)this.status) + ", origemPedido=" + String.valueOf((Object)this.origemPedido) + ", responsavelAtendimento='" + this.responsavelAtendimento + "', entregador='" + this.entregador + "', modoEntrega=" + String.valueOf((Object)this.modoEntrega) + ", codigoRastreamento='" + this.codigoRastreamento + "', dataEntregaEstimada=" + String.valueOf(this.dataEntregaEstimada) + ", canalAtendimento=" + String.valueOf((Object)this.canalAtendimento) + ", mensagemCliente='" + this.mensagemCliente + "', mensagemInterna='" + this.mensagemInterna + "'}";
    }

    public static enum StatusVenda {
        PENDENTE,
        CONFIRMADA,
        CANCELADA,
        ESTORNADA;

    }

    public static enum OrigemPedido {
        SITE,
        BALCAO,
        DELIVERY,
        APP;

    }

    public static enum ModoEntrega {
        RETIRADA,
        ENTREGA;

    }

    public static enum CanalAtendimento {
        SITE,
        BALCAO,
        DELIVERY,
        APP;

    }
}

