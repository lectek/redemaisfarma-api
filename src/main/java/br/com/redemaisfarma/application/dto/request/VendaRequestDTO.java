package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de requisição para registrar uma venda. Garante coerência entre itens, totais e formas de pagamento.
 */
@Schema(name = "VendaRequestDTO", description = "Dados para registro de venda")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendaRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do cliente", example = "123", required = true)
    @NotNull(message = "{venda.clienteId.notNull}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Itens da venda", required = true)
    @NotEmpty(message = "{venda.itens.notEmpty}")
    @Valid
    @JsonProperty("itens")
    private List<@Valid ItemVendaDTO> itens;

    @Schema(description = "Valor total bruto (soma dos itens)", example = "200.00", required = true)
    @NotNull(message = "{venda.totalBruto.notNull}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{venda.totalBruto.min}")
    @JsonProperty("totalBruto")
    private BigDecimal totalBruto;

    @Schema(description = "Total de descontos aplicados", example = "20.00")
    @DecimalMin(value = "0.00", inclusive = true, message = "{venda.totalDescontos.min}")
    @JsonProperty("totalDescontos")
    private BigDecimal totalDescontos;

    @Schema(description = "Total líquido (bruto - descontos)", example = "180.00", required = true)
    @NotNull(message = "{venda.totalLiquido.notNull}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{venda.totalLiquido.min}")
    @JsonProperty("totalLiquido")
    private BigDecimal totalLiquido;

    @Schema(description = "Formas de pagamento da venda", required = true)
    @NotEmpty(message = "{venda.formasPagamento.notEmpty}")
    @Valid
    @JsonProperty("formasPagamento")
    private List<@Valid FormaPagamentoRequestDTO> formasPagamento;

    @Schema(description = "Tipo da venda (PRESENCIAL ou ONLINE)", example = "PRESENCIAL", required = true)
    @NotNull(message = "{venda.tipoVenda.notNull}")
    @JsonProperty("tipoVenda")
    private TipoVenda tipoVenda;

    @Schema(description = "ID do ponto de venda (obrigatório para PRESENCIAL)", example = "10")
    @JsonProperty("pontoVendaId")
    private Long pontoVendaId;

    @Schema(description = "Data/hora da venda", type = "string", format = "date-time", example = "2025-07-04T10:15:30")
    @NotNull(message = "{venda.dataVenda.notNull}")
    @PastOrPresent(message = "{venda.dataVenda.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataVenda")
    private LocalDateTime dataVenda;

    @Schema(description = "Observações adicionais", example = "Entregar em domicílio na parte da tarde")
    @Size(max = 500, message = "{venda.observacao.size}")
    @JsonProperty("observacao")
    private String observacao;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{venda.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID) para auditoria", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    /*
     * ====================== Regras de consistência ======================
     */

    /**
     * Total líquido deve ser exatamente total bruto - descontos.
     */
    @AssertTrue(message = "{venda.totalLiquido.coerencia}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isTotaisCoerentes() {
        if (totalBruto == null || totalLiquido == null)
            return false;
        if (totalBruto.signum() < 0 || totalLiquido.signum() <= 0)
            return false;

        BigDecimal descontos = totalDescontos != null ? totalDescontos : BigDecimal.ZERO;
        if (descontos.signum() < 0 || descontos.compareTo(totalBruto) > 0)
            return false;

        return totalBruto.subtract(descontos).compareTo(totalLiquido) == 0;
    }

    /**
     * Soma das formas de pagamento deve ser igual ao total líquido.
     */
    @AssertTrue(message = "{venda.formasPagamento.somaIgualTotalLiquido}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isSomaFormasPagamentoValida() {
        if (formasPagamento == null || formasPagamento.isEmpty() || totalLiquido == null)
            return false;

        BigDecimal soma = formasPagamento.stream().map(FormaPagamentoRequestDTO::getValor).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return soma.compareTo(totalLiquido) == 0;
    }

    /**
     * Para venda PRESENCIAL, pontoVendaId é obrigatório.
     */
    @AssertTrue(message = "{venda.pontoVenda.obrigatorio.presencial}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isPontoVendaValido() {
        if (tipoVenda == null)
            return false;
        if (tipoVenda == TipoVenda.PRESENCIAL)
            return pontoVendaId != null;
        return true; // ONLINE não exige ponto de venda
    }

    /**
     * (Opcional, mas recomendado) Verifica se a soma dos subtotais dos itens é igual ao total bruto. Mantém tudo
     * consistente ponta-a-ponta.
     */
    @AssertTrue(message = "{venda.itens.somaIgualTotalBruto}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isSomaItensIgualTotalBruto() {
        if (itens == null || itens.isEmpty() || totalBruto == null)
            return false;

        BigDecimal soma = itens.stream().map(ItemVendaDTO::getSubtotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        return soma.compareTo(totalBruto) == 0;
    }

    /*
     * ====================== Getters e Setters ======================
     */

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemVendaDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemVendaDTO> itens) {
        this.itens = itens;
    }

    public BigDecimal getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(BigDecimal totalBruto) {
        this.totalBruto = totalBruto;
    }

    public BigDecimal getTotalDescontos() {
        return totalDescontos;
    }

    public void setTotalDescontos(BigDecimal totalDescontos) {
        this.totalDescontos = totalDescontos;
    }

    public BigDecimal getTotalLiquido() {
        return totalLiquido;
    }

    public void setTotalLiquido(BigDecimal totalLiquido) {
        this.totalLiquido = totalLiquido;
    }

    public List<FormaPagamentoRequestDTO> getFormasPagamento() {
        return formasPagamento;
    }

    public void setFormasPagamento(List<FormaPagamentoRequestDTO> formasPagamento) {
        this.formasPagamento = formasPagamento;
    }

    public TipoVenda getTipoVenda() {
        return tipoVenda;
    }

    public void setTipoVenda(TipoVenda tipoVenda) {
        this.tipoVenda = tipoVenda;
    }

    public Long getPontoVendaId() {
        return pontoVendaId;
    }

    public void setPontoVendaId(Long pontoVendaId) {
        this.pontoVendaId = pontoVendaId;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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

    /*
     * ====================== Tipos auxiliares ======================
     */

    public enum TipoVenda {
        PRESENCIAL, ONLINE
    }

    /**
     * DTO de item da venda (request). Se você já possui um DTO próprio, pode remover este interno e usar o seu.
     */
    @Schema(name = "ItemVendaDTO", description = "Item de venda")
    public static class ItemVendaDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID do produto", example = "987", required = true)
        @NotNull(message = "{venda.item.produtoId.notNull}")
        private Long produtoId;

        @Schema(description = "Quantidade do item", example = "2", required = true)
        @NotNull(message = "{venda.item.quantidade.notNull}")
        @Min(value = 1, message = "{venda.item.quantidade.min}")
        private Integer quantidade;

        @Schema(description = "Preço unitário", example = "100.00", required = true)
        @NotNull(message = "{venda.item.precoUnitario.notNull}")
        @DecimalMin(value = "0.00", inclusive = false, message = "{venda.item.precoUnitario.min}")
        private BigDecimal precoUnitario;

        @Schema(description = "Subtotal do item (quantidade * preço unitário)", example = "200.00", required = true)
        @NotNull(message = "{venda.item.subtotal.notNull}")
        @DecimalMin(value = "0.00", inclusive = false, message = "{venda.item.subtotal.min}")
        private BigDecimal subtotal;

        /* Coerência mínima por item: subtotal = quantidade * preço */
        @AssertTrue(message = "{venda.item.subtotal.coerencia}")
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public boolean isSubtotalCoerente() {
            if (quantidade == null || precoUnitario == null || subtotal == null)
                return false;
            BigDecimal esperado = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
            return esperado.compareTo(subtotal) == 0;
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
        }
    }
}
