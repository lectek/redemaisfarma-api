package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de resposta para relatórios gerenciais, financeiros e operacionais.
 */
@Schema(name = "FiltroRelatorioResponseDTO", description = "Resultado do relatório com dados agregados e detalhes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FiltroRelatorioResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único da geração do relatório", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("relatorioId")
    private UUID relatorioId;

    @Schema(description = "Timestamp de geração do relatório", type = "string", format = "date-time", example = "2025-07-04T16:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataGeracao")
    private LocalDateTime dataGeracao;

    @Schema(description = "Username do solicitante", example = "joao.silva")
    @JsonProperty("usuarioSolicitante")
    private String usuarioSolicitante;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Filtros aplicados (campo -> valor)", example = "{\"dataInicial\":\"2025-01-01\",\"dataFinal\":\"2025-06-30\"}")
    @JsonProperty("filtrosAplicados")
    private Map<String, String> filtrosAplicados;

    @Schema(description = "Tempo de processamento (ms)", example = "1234")
    @JsonProperty("tempoProcessamentoMs")
    private Long tempoProcessamentoMs;

    // Observação: mantido o nome 'metricasAggregadas' para não quebrar consumo existente.
    @Schema(description = "Métricas agregadas como chave-valor", example = "{\"totalVendas\":1000.50,\"numeroPedidos\":150}")
    @JsonProperty("metricasAggregadas")
    private Map<String, BigDecimal> metricasAggregadas;

    @Schema(description = "Informações de paginação do resultado")
    @JsonProperty("paginacao")
    private PaginacaoDTO paginacao;

    @Schema(description = "Lista de critérios de ordenação", example = "[\"dataVenda,DESC\"]")
    @JsonProperty("ordenacao")
    private List<String> ordenacao;

    @Schema(description = "Lista de vendas detalhadas")
    @JsonProperty("detalhesVendas")
    private List<VendaDTO> detalhesVendas;

    @Schema(description = "Lista de produtos detalhados")
    @JsonProperty("detalhesProdutos")
    private List<ProdutoDTO> detalhesProdutos;

    @Schema(description = "Lista de clientes detalhados")
    @JsonProperty("detalhesClientes")
    private List<ClienteResumoDTO> detalhesClientes;

    @Schema(description = "Lista de atendentes detalhados")
    @JsonProperty("detalhesAtendentes")
    private List<AtendenteDTO> detalhesAtendentes;

    // Construtores
    public FiltroRelatorioResponseDTO() {
    }

    public FiltroRelatorioResponseDTO(UUID relatorioId, LocalDateTime dataGeracao, String usuarioSolicitante,
            String tenantId, Map<String, String> filtrosAplicados, Long tempoProcessamentoMs,
            Map<String, BigDecimal> metricasAggregadas, PaginacaoDTO paginacao, List<String> ordenacao,
            List<VendaDTO> detalhesVendas, List<ProdutoDTO> detalhesProdutos, List<ClienteResumoDTO> detalhesClientes,
            List<AtendenteDTO> detalhesAtendentes) {
        this.relatorioId = relatorioId;
        this.dataGeracao = dataGeracao;
        this.usuarioSolicitante = usuarioSolicitante;
        this.tenantId = tenantId;
        this.filtrosAplicados = filtrosAplicados;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
        this.metricasAggregadas = metricasAggregadas;
        this.paginacao = paginacao;
        this.ordenacao = ordenacao;
        this.detalhesVendas = detalhesVendas;
        this.detalhesProdutos = detalhesProdutos;
        this.detalhesClientes = detalhesClientes;
        this.detalhesAtendentes = detalhesAtendentes;
    }

    // Getters/Setters
    public UUID getRelatorioId() {
        return relatorioId;
    }

    public void setRelatorioId(UUID relatorioId) {
        this.relatorioId = relatorioId;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
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

    public Map<String, String> getFiltrosAplicados() {
        return filtrosAplicados;
    }

    public void setFiltrosAplicados(Map<String, String> filtrosAplicados) {
        this.filtrosAplicados = filtrosAplicados;
    }

    public Long getTempoProcessamentoMs() {
        return tempoProcessamentoMs;
    }

    public void setTempoProcessamentoMs(Long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }

    public Map<String, BigDecimal> getMetricasAggregadas() {
        return metricasAggregadas;
    }

    public void setMetricasAggregadas(Map<String, BigDecimal> metricasAggregadas) {
        this.metricasAggregadas = metricasAggregadas;
    }

    public PaginacaoDTO getPaginacao() {
        return paginacao;
    }

    public void setPaginacao(PaginacaoDTO paginacao) {
        this.paginacao = paginacao;
    }

    public List<String> getOrdenacao() {
        return ordenacao;
    }

    public void setOrdenacao(List<String> ordenacao) {
        this.ordenacao = ordenacao;
    }

    public List<VendaDTO> getDetalhesVendas() {
        return detalhesVendas;
    }

    public void setDetalhesVendas(List<VendaDTO> detalhesVendas) {
        this.detalhesVendas = detalhesVendas;
    }

    public List<ProdutoDTO> getDetalhesProdutos() {
        return detalhesProdutos;
    }

    public void setDetalhesProdutos(List<ProdutoDTO> detalhesProdutos) {
        this.detalhesProdutos = detalhesProdutos;
    }

    public List<ClienteResumoDTO> getDetalhesClientes() {
        return detalhesClientes;
    }

    public void setDetalhesClientes(List<ClienteResumoDTO> detalhesClientes) {
        this.detalhesClientes = detalhesClientes;
    }

    public List<AtendenteDTO> getDetalhesAtendentes() {
        return detalhesAtendentes;
    }

    public void setDetalhesAtendentes(List<AtendenteDTO> detalhesAtendentes) {
        this.detalhesAtendentes = detalhesAtendentes;
    }

    // equals/hashCode/toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FiltroRelatorioResponseDTO that))
            return false;
        return Objects.equals(relatorioId, that.relatorioId) && Objects.equals(dataGeracao, that.dataGeracao)
                && Objects.equals(usuarioSolicitante, that.usuarioSolicitante)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(filtrosAplicados, that.filtrosAplicados)
                && Objects.equals(tempoProcessamentoMs, that.tempoProcessamentoMs)
                && Objects.equals(metricasAggregadas, that.metricasAggregadas)
                && Objects.equals(paginacao, that.paginacao) && Objects.equals(ordenacao, that.ordenacao)
                && Objects.equals(detalhesVendas, that.detalhesVendas)
                && Objects.equals(detalhesProdutos, that.detalhesProdutos)
                && Objects.equals(detalhesClientes, that.detalhesClientes)
                && Objects.equals(detalhesAtendentes, that.detalhesAtendentes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relatorioId, dataGeracao, usuarioSolicitante, tenantId, filtrosAplicados,
                tempoProcessamentoMs, metricasAggregadas, paginacao, ordenacao, detalhesVendas, detalhesProdutos,
                detalhesClientes, detalhesAtendentes);
    }

    @Override
    public String toString() {
        return "FiltroRelatorioResponseDTO{" + "relatorioId=" + relatorioId + ", dataGeracao=" + dataGeracao
                + ", usuarioSolicitante='" + usuarioSolicitante + '\'' + ", tenantId='" + tenantId + '\''
                + ", filtrosAplicados=" + filtrosAplicados + ", tempoProcessamentoMs=" + tempoProcessamentoMs
                + ", metricasAggregadas=" + metricasAggregadas + ", paginacao=" + paginacao + ", ordenacao=" + ordenacao
                + ", detalhesVendas=" + detalhesVendas + ", detalhesProdutos=" + detalhesProdutos
                + ", detalhesClientes=" + detalhesClientes + ", detalhesAtendentes=" + detalhesAtendentes + '}';
    }

    // =======================================
    // DTOs internos
    // =======================================

    @Schema(name = "PaginacaoDTO", description = "Dados de paginação dos resultados")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginacaoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Página atual (zero-based)", example = "0")
        @JsonProperty("pagina")
        private Integer pagina;

        @Schema(description = "Tamanho da página", example = "50")
        @JsonProperty("tamanho")
        private Integer tamanho;

        @Schema(description = "Total de páginas", example = "20")
        @JsonProperty("totalPaginas")
        private Integer totalPaginas;

        @Schema(description = "Total de registros", example = "1000")
        @JsonProperty("totalRegistros")
        private Long totalRegistros;

        public PaginacaoDTO() {
        }

        public PaginacaoDTO(Integer pagina, Integer tamanho, Integer totalPaginas, Long totalRegistros) {
            this.pagina = pagina;
            this.tamanho = tamanho;
            this.totalPaginas = totalPaginas;
            this.totalRegistros = totalRegistros;
        }

        public Integer getPagina() {
            return pagina;
        }

        public void setPagina(Integer pagina) {
            this.pagina = pagina;
        }

        public Integer getTamanho() {
            return tamanho;
        }

        public void setTamanho(Integer tamanho) {
            this.tamanho = tamanho;
        }

        public Integer getTotalPaginas() {
            return totalPaginas;
        }

        public void setTotalPaginas(Integer totalPaginas) {
            this.totalPaginas = totalPaginas;
        }

        public Long getTotalRegistros() {
            return totalRegistros;
        }

        public void setTotalRegistros(Long totalRegistros) {
            this.totalRegistros = totalRegistros;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PaginacaoDTO that))
                return false;
            return Objects.equals(pagina, that.pagina) && Objects.equals(tamanho, that.tamanho)
                    && Objects.equals(totalPaginas, that.totalPaginas)
                    && Objects.equals(totalRegistros, that.totalRegistros);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pagina, tamanho, totalPaginas, totalRegistros);
        }

        @Override
        public String toString() {
            return "PaginacaoDTO{" + "pagina=" + pagina + ", tamanho=" + tamanho + ", totalPaginas=" + totalPaginas
                    + ", totalRegistros=" + totalRegistros + '}';
        }
    }

    @Schema(name = "VendaDTO", description = "Dados resumidos de cada venda")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VendaDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID da venda", example = "456")
        @JsonProperty("vendaId")
        private UUID vendaId;

        @Schema(description = "Data da venda", type = "string", format = "date-time", example = "2025-07-01T10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("dataVenda")
        private LocalDateTime dataVenda;

        @Schema(description = "Valor total da venda", example = "200.00")
        @JsonProperty("valorVenda")
        private BigDecimal valorVenda;

        public VendaDTO() {
        }

        public VendaDTO(UUID vendaId, LocalDateTime dataVenda, BigDecimal valorVenda) {
            this.vendaId = vendaId;
            this.dataVenda = dataVenda;
            this.valorVenda = valorVenda;
        }

        public UUID getVendaId() {
            return vendaId;
        }

        public void setVendaId(UUID vendaId) {
            this.vendaId = vendaId;
        }

        public LocalDateTime getDataVenda() {
            return dataVenda;
        }

        public void setDataVenda(LocalDateTime dataVenda) {
            this.dataVenda = dataVenda;
        }

        public BigDecimal getValorVenda() {
            return valorVenda;
        }

        public void setValorVenda(BigDecimal valorVenda) {
            this.valorVenda = valorVenda;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof VendaDTO that))
                return false;
            return Objects.equals(vendaId, that.vendaId) && Objects.equals(dataVenda, that.dataVenda)
                    && Objects.equals(valorVenda, that.valorVenda);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vendaId, dataVenda, valorVenda);
        }

        @Override
        public String toString() {
            return "VendaDTO{" + "vendaId=" + vendaId + ", dataVenda=" + dataVenda + ", valorVenda=" + valorVenda + '}';
        }
    }

    @Schema(name = "ProdutoDTO", description = "Dados resumidos de cada produto")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProdutoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID do produto", example = "789")
        @JsonProperty("produtoId")
        private Long produtoId;

        @Schema(description = "Nome do produto", example = "Dipirona 500mg")
        @JsonProperty("nome")
        private String nome;

        @Schema(description = "Categoria do produto", example = "ANALGESICO")
        @JsonProperty("categoria")
        private String categoria;

        @Schema(description = "Valor total vendido (no período)", example = "1234.56")
        @JsonProperty("valorTotalVendido")
        private BigDecimal valorTotalVendido;

        public ProdutoDTO() {
        }

        public ProdutoDTO(Long produtoId, String nome, String categoria, BigDecimal valorTotalVendido) {
            this.produtoId = produtoId;
            this.nome = nome;
            this.categoria = categoria;
            this.valorTotalVendido = valorTotalVendido;
        }

        public Long getProdutoId() {
            return produtoId;
        }

        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public BigDecimal getValorTotalVendido() {
            return valorTotalVendido;
        }

        public void setValorTotalVendido(BigDecimal valorTotalVendido) {
            this.valorTotalVendido = valorTotalVendido;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ProdutoDTO that))
                return false;
            return Objects.equals(produtoId, that.produtoId) && Objects.equals(nome, that.nome)
                    && Objects.equals(categoria, that.categoria)
                    && Objects.equals(valorTotalVendido, that.valorTotalVendido);
        }

        @Override
        public int hashCode() {
            return Objects.hash(produtoId, nome, categoria, valorTotalVendido);
        }

        @Override
        public String toString() {
            return "ProdutoDTO{" + "produtoId=" + produtoId + ", nome='" + nome + '\'' + ", categoria='" + categoria
                    + '\'' + ", valorTotalVendido=" + valorTotalVendido + '}';
        }
    }
}
