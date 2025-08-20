package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para relatórios financeiros e operacionais da API RedeMaisFarma.
 *
 * Contém métricas agregadas, detalhes operacionais, filtros aplicados, metadados de auditoria e configurações de
 * exportação — pronto para dashboards, mobile e exportação.
 */
@Schema(name = "RelatorioResponseDTO", description = "Dados agregados do relatório gerado")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelatorioResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Período e filtros utilizados
    // ----------------------------------------------------------------------

    @Schema(description = "Data de início do relatório", type = "string", format = "date", example = "2025-07-01", required = true)
    @NotNull(message = "{relatorio.dataInicio.notNull}")
    @JsonProperty("dataInicio")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;

    @Schema(description = "Data de fim do relatório", type = "string", format = "date", example = "2025-07-31", required = true)
    @NotNull(message = "{relatorio.dataFim.notNull}")
    @JsonProperty("dataFim")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFim;

    @Schema(description = "Categoria principal de filtro", example = "FARMACEUTICOS")
    @Size(max = 100, message = "{relatorio.categoriaFiltro.size}")
    @JsonProperty("categoriaFiltro")
    private String categoriaFiltro;

    // ----------------------------------------------------------------------
    // Métricas agregadas
    // ----------------------------------------------------------------------

    @Schema(description = "Total de vendas no período", example = "15000.50", required = true)
    @NotNull(message = "{relatorio.totalVendas.notNull}")
    @DecimalMin(value = "0.00", inclusive = true, message = "{relatorio.totalVendas.min}")
    @Digits(integer = 14, fraction = 2, message = "{relatorio.totalVendas.digits}")
    @JsonProperty("totalVendas")
    private BigDecimal totalVendas;

    @Schema(description = "Margem de lucro (%)", example = "25.50", required = true)
    @NotNull(message = "{relatorio.margemLucro.notNull}")
    @DecimalMin(value = "0.00", inclusive = true, message = "{relatorio.margemLucro.min}")
    @DecimalMax(value = "100.00", inclusive = true, message = "{relatorio.margemLucro.max}")
    @Digits(integer = 3, fraction = 2, message = "{relatorio.margemLucro.digits}")
    @JsonProperty("margemLucro")
    private BigDecimal margemLucro;

    @Schema(description = "Quantidade de produtos vendidos", example = "1200", required = true)
    @NotNull(message = "{relatorio.quantidadeProdutosVendidos.notNull}")
    @Min(value = 0, message = "{relatorio.quantidadeProdutosVendidos.min}")
    @JsonProperty("quantidadeProdutosVendidos")
    private Long quantidadeProdutosVendidos;

    @Schema(description = "Total de pedidos realizados", example = "300", required = true)
    @NotNull(message = "{relatorio.totalPedidos.notNull}")
    @Min(value = 0, message = "{relatorio.totalPedidos.min}")
    @JsonProperty("totalPedidos")
    private Long totalPedidos;

    @Schema(description = "Ticket médio", example = "50.25")
    @DecimalMin(value = "0.00", inclusive = true, message = "{relatorio.mediaTicket.min}")
    @Digits(integer = 14, fraction = 2, message = "{relatorio.mediaTicket.digits}")
    @JsonProperty("mediaTicket")
    private BigDecimal mediaTicket;

    // ----------------------------------------------------------------------
    // Detalhamentos por dimensão
    // ----------------------------------------------------------------------

    @Schema(description = "Vendas por categoria (categoria -> valor)", example = "{\"REMEDIOS\":10000.00,\"PERFUMARIA\":5000.50}")
    @JsonProperty("vendasPorCategoria")
    private Map<@NotBlank @Size(max = 100) String, @NotNull @Digits(integer = 14, fraction = 2) BigDecimal> vendasPorCategoria;

    @Schema(description = "Lucro por produto (produtoId -> valor)", example = "{\"001\":2000.00,\"002\":1500.00}")
    @JsonProperty("lucroPorProduto")
    private Map<@NotBlank @Size(max = 64) String, @NotNull @Digits(integer = 14, fraction = 2) BigDecimal> lucroPorProduto;

    @Schema(description = "Produtos mais vendidos no período")
    @JsonProperty("produtosMaisVendidos")
    private List<@NotBlank @Size(max = 150) String> produtosMaisVendidos;

    // ----------------------------------------------------------------------
    // Metadados de geração e auditoria
    // ----------------------------------------------------------------------

    @Schema(description = "Data/hora de geração do relatório", type = "string", format = "date-time", example = "2025-08-01T08:00:00", required = true)
    @NotNull(message = "{relatorio.dataGeracaoRelatorio.notNull}")
    @PastOrPresent(message = "{relatorio.dataGeracaoRelatorio.pastOrPresent}")
    @JsonProperty("dataGeracaoRelatorio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataGeracaoRelatorio;

    @Schema(description = "Usuário solicitante", example = "joao.silva", required = true)
    @NotBlank(message = "{relatorio.usuarioSolicitante.notBlank}")
    @Size(max = 100, message = "{relatorio.usuarioSolicitante.size}")
    @JsonProperty("usuarioSolicitante")
    private String usuarioSolicitante;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{relatorio.tenantId.notBlank}")
    @Size(max = 100, message = "{relatorio.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de correlação (UUID)", example = "5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Duração da geração do relatório (ms)", example = "1500")
    @PositiveOrZero(message = "{relatorio.duracaoConsulta.min}")
    @JsonProperty("duracaoConsulta")
    private Long duracaoConsulta;

    @Schema(description = "Método de exportação do relatório", example = "PDF", allowableValues = { "PDF", "EXCEL",
            "CSV", "JSON" })
    @JsonProperty("metodoExportacao")
    private MetodoExportacao metodoExportacao;

    @Schema(description = "Status da geração do relatório", example = "SUCCESS", required = true, allowableValues = {
            "SUCCESS", "EMPTY", "ERROR" })
    @NotNull(message = "{relatorio.statusConsulta.notNull}")
    @JsonProperty("statusConsulta")
    private StatusConsulta statusConsulta;

    // ----------------------------------------------------------------------
    // Validações compostas
    // ----------------------------------------------------------------------

    @AssertTrue(message = "{relatorio.periodo.valido}")
    public boolean isPeriodoValido() {
        if (dataInicio == null || dataFim == null)
            return true; // outras anotações já validam null
        return !dataFim.isBefore(dataInicio);
    }

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------

    public RelatorioResponseDTO() {
    }

    public RelatorioResponseDTO(LocalDate dataInicio, LocalDate dataFim, String categoriaFiltro, BigDecimal totalVendas,
            BigDecimal margemLucro, Long quantidadeProdutosVendidos, Long totalPedidos, BigDecimal mediaTicket,
            Map<String, BigDecimal> vendasPorCategoria, Map<String, BigDecimal> lucroPorProduto,
            List<String> produtosMaisVendidos, LocalDateTime dataGeracaoRelatorio, String usuarioSolicitante,
            String tenantId, UUID traceId, Long duracaoConsulta, MetodoExportacao metodoExportacao,
            StatusConsulta statusConsulta) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.categoriaFiltro = categoriaFiltro;
        this.totalVendas = totalVendas;
        this.margemLucro = margemLucro;
        this.quantidadeProdutosVendidos = quantidadeProdutosVendidos;
        this.totalPedidos = totalPedidos;
        this.mediaTicket = mediaTicket;
        this.vendasPorCategoria = vendasPorCategoria;
        this.lucroPorProduto = lucroPorProduto;
        this.produtosMaisVendidos = produtosMaisVendidos;
        this.dataGeracaoRelatorio = dataGeracaoRelatorio;
        this.usuarioSolicitante = usuarioSolicitante;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.duracaoConsulta = duracaoConsulta;
        this.metodoExportacao = metodoExportacao;
        this.statusConsulta = statusConsulta;
    }

    // ----------------------------------------------------------------------
    // Getters e Setters
    // ----------------------------------------------------------------------

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getCategoriaFiltro() {
        return categoriaFiltro;
    }

    public void setCategoriaFiltro(String categoriaFiltro) {
        this.categoriaFiltro = categoriaFiltro;
    }

    public BigDecimal getTotalVendas() {
        return totalVendas;
    }

    public void setTotalVendas(BigDecimal totalVendas) {
        this.totalVendas = totalVendas;
    }

    public BigDecimal getMargemLucro() {
        return margemLucro;
    }

    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
    }

    public Long getQuantidadeProdutosVendidos() {
        return quantidadeProdutosVendidos;
    }

    public void setQuantidadeProdutosVendidos(Long quantidadeProdutosVendidos) {
        this.quantidadeProdutosVendidos = quantidadeProdutosVendidos;
    }

    public Long getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(Long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public BigDecimal getMediaTicket() {
        return mediaTicket;
    }

    public void setMediaTicket(BigDecimal mediaTicket) {
        this.mediaTicket = mediaTicket;
    }

    public Map<String, BigDecimal> getVendasPorCategoria() {
        return vendasPorCategoria;
    }

    public void setVendasPorCategoria(Map<String, BigDecimal> vendasPorCategoria) {
        this.vendasPorCategoria = vendasPorCategoria;
    }

    public Map<String, BigDecimal> getLucroPorProduto() {
        return lucroPorProduto;
    }

    public void setLucroPorProduto(Map<String, BigDecimal> lucroPorProduto) {
        this.lucroPorProduto = lucroPorProduto;
    }

    public List<String> getProdutosMaisVendidos() {
        return produtosMaisVendidos;
    }

    public void setProdutosMaisVendidos(List<String> produtosMaisVendidos) {
        this.produtosMaisVendidos = produtosMaisVendidos;
    }

    public LocalDateTime getDataGeracaoRelatorio() {
        return dataGeracaoRelatorio;
    }

    public void setDataGeracaoRelatorio(LocalDateTime dataGeracaoRelatorio) {
        this.dataGeracaoRelatorio = dataGeracaoRelatorio;
    }

    public String getUsuarioSolicitante() {
        return usuarioSolicitante;
    }

    public void setUsuarioSolicitante(String usuarioSolicitante) {
        this.usuarioSolicitante = usuarioSolicitante;
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

    public Long getDuracaoConsulta() {
        return duracaoConsulta;
    }

    public void setDuracaoConsulta(Long duracaoConsulta) {
        this.duracaoConsulta = duracaoConsulta;
    }

    public MetodoExportacao getMetodoExportacao() {
        return metodoExportacao;
    }

    public void setMetodoExportacao(MetodoExportacao metodoExportacao) {
        this.metodoExportacao = metodoExportacao;
    }

    public StatusConsulta getStatusConsulta() {
        return statusConsulta;
    }

    public void setStatusConsulta(StatusConsulta statusConsulta) {
        this.statusConsulta = statusConsulta;
    }

    // ----------------------------------------------------------------------
    // Métodos utilitários
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RelatorioResponseDTO))
            return false;
        RelatorioResponseDTO that = (RelatorioResponseDTO) o;
        return Objects.equals(dataInicio, that.dataInicio) && Objects.equals(dataFim, that.dataFim)
                && Objects.equals(categoriaFiltro, that.categoriaFiltro)
                && Objects.equals(totalVendas, that.totalVendas) && Objects.equals(margemLucro, that.margemLucro)
                && Objects.equals(quantidadeProdutosVendidos, that.quantidadeProdutosVendidos)
                && Objects.equals(totalPedidos, that.totalPedidos) && Objects.equals(mediaTicket, that.mediaTicket)
                && Objects.equals(vendasPorCategoria, that.vendasPorCategoria)
                && Objects.equals(lucroPorProduto, that.lucroPorProduto)
                && Objects.equals(produtosMaisVendidos, that.produtosMaisVendidos)
                && Objects.equals(dataGeracaoRelatorio, that.dataGeracaoRelatorio)
                && Objects.equals(usuarioSolicitante, that.usuarioSolicitante)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(duracaoConsulta, that.duracaoConsulta) && metodoExportacao == that.metodoExportacao
                && statusConsulta == that.statusConsulta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataInicio, dataFim, categoriaFiltro, totalVendas, margemLucro, quantidadeProdutosVendidos,
                totalPedidos, mediaTicket, vendasPorCategoria, lucroPorProduto, produtosMaisVendidos,
                dataGeracaoRelatorio, usuarioSolicitante, tenantId, traceId, duracaoConsulta, metodoExportacao,
                statusConsulta);
    }

    @Override
    public String toString() {
        return "RelatorioResponseDTO{" + "dataInicio=" + dataInicio + ", dataFim=" + dataFim + ", categoriaFiltro='"
                + categoriaFiltro + '\'' + ", totalVendas=" + totalVendas + ", margemLucro=" + margemLucro
                + ", quantidadeProdutosVendidos=" + quantidadeProdutosVendidos + ", totalPedidos=" + totalPedidos
                + ", mediaTicket=" + mediaTicket + ", vendasPorCategoria=" + vendasPorCategoria + ", lucroPorProduto="
                + lucroPorProduto + ", produtosMaisVendidos=" + produtosMaisVendidos + ", dataGeracaoRelatorio="
                + dataGeracaoRelatorio + ", usuarioSolicitante='" + usuarioSolicitante + '\'' + ", tenantId='"
                + tenantId + '\'' + ", traceId=" + traceId + ", duracaoConsulta=" + duracaoConsulta
                + ", metodoExportacao=" + metodoExportacao + ", statusConsulta=" + statusConsulta + '}';
    }

    // Enums
    public enum MetodoExportacao {
        PDF, EXCEL, CSV, JSON
    }

    public enum StatusConsulta {
        SUCCESS, EMPTY, ERROR
    }
}
