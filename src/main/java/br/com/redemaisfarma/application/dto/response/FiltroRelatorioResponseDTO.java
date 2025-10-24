/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.redemaisfarma.application.dto.response;

import br.com.redemaisfarma.application.dto.response.AtendenteDTO;
import br.com.redemaisfarma.application.dto.response.ClienteResumoDTO;
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

@Schema(name="FiltroRelatorioResponseDTO", description="Resultado do relat\u00f3rio com dados agregados e detalhes")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class FiltroRelatorioResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico da gera\u00e7\u00e3o do relat\u00f3rio", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="relatorioId")
    private UUID relatorioId;
    @Schema(description="Timestamp de gera\u00e7\u00e3o do relat\u00f3rio", type="string", format="date-time", example="2025-07-04T16:30:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataGeracao")
    private LocalDateTime dataGeracao;
    @Schema(description="Username do solicitante", example="joao.silva")
    @JsonProperty(value="usuarioSolicitante")
    private String usuarioSolicitante;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001")
    @JsonProperty(value="tenantId")
    private String tenantId;
    @Schema(description="Filtros aplicados (campo -> valor)", example="{\"dataInicial\":\"2025-01-01\",\"dataFinal\":\"2025-06-30\"}")
    @JsonProperty(value="filtrosAplicados")
    private Map<String, String> filtrosAplicados;
    @Schema(description="Tempo de processamento (ms)", example="1234")
    @JsonProperty(value="tempoProcessamentoMs")
    private Long tempoProcessamentoMs;
    @Schema(description="M\u00e9tricas agregadas como chave-valor", example="{\"totalVendas\":1000.50,\"numeroPedidos\":150}")
    @JsonProperty(value="metricasAggregadas")
    private Map<String, BigDecimal> metricasAggregadas;
    @Schema(description="Informa\u00e7\u00f5es de pagina\u00e7\u00e3o do resultado")
    @JsonProperty(value="paginacao")
    private PaginacaoDTO paginacao;
    @Schema(description="Lista de crit\u00e9rios de ordena\u00e7\u00e3o", example="[\"dataVenda,DESC\"]")
    @JsonProperty(value="ordenacao")
    private List<String> ordenacao;
    @Schema(description="Lista de vendas detalhadas")
    @JsonProperty(value="detalhesVendas")
    private List<VendaDTO> detalhesVendas;
    @Schema(description="Lista de produtos detalhados")
    @JsonProperty(value="detalhesProdutos")
    private List<ProdutoDTO> detalhesProdutos;
    @Schema(description="Lista de clientes detalhados")
    @JsonProperty(value="detalhesClientes")
    private List<ClienteResumoDTO> detalhesClientes;
    @Schema(description="Lista de atendentes detalhados")
    @JsonProperty(value="detalhesAtendentes")
    private List<AtendenteDTO> detalhesAtendentes;

    public FiltroRelatorioResponseDTO() {
    }

    public FiltroRelatorioResponseDTO(UUID relatorioId, LocalDateTime dataGeracao, String usuarioSolicitante, String tenantId, Map<String, String> filtrosAplicados, Long tempoProcessamentoMs, Map<String, BigDecimal> metricasAggregadas, PaginacaoDTO paginacao, List<String> ordenacao, List<VendaDTO> detalhesVendas, List<ProdutoDTO> detalhesProdutos, List<ClienteResumoDTO> detalhesClientes, List<AtendenteDTO> detalhesAtendentes) {
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

    public UUID getRelatorioId() {
        return this.relatorioId;
    }

    public void setRelatorioId(UUID relatorioId) {
        this.relatorioId = relatorioId;
    }

    public LocalDateTime getDataGeracao() {
        return this.dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public String getUsuarioSolicitante() {
        return this.usuarioSolicitante;
    }

    public void setUsuarioSolicitante(String usuarioSolicitante) {
        this.usuarioSolicitante = usuarioSolicitante;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Map<String, String> getFiltrosAplicados() {
        return this.filtrosAplicados;
    }

    public void setFiltrosAplicados(Map<String, String> filtrosAplicados) {
        this.filtrosAplicados = filtrosAplicados;
    }

    public Long getTempoProcessamentoMs() {
        return this.tempoProcessamentoMs;
    }

    public void setTempoProcessamentoMs(Long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }

    public Map<String, BigDecimal> getMetricasAggregadas() {
        return this.metricasAggregadas;
    }

    public void setMetricasAggregadas(Map<String, BigDecimal> metricasAggregadas) {
        this.metricasAggregadas = metricasAggregadas;
    }

    public PaginacaoDTO getPaginacao() {
        return this.paginacao;
    }

    public void setPaginacao(PaginacaoDTO paginacao) {
        this.paginacao = paginacao;
    }

    public List<String> getOrdenacao() {
        return this.ordenacao;
    }

    public void setOrdenacao(List<String> ordenacao) {
        this.ordenacao = ordenacao;
    }

    public List<VendaDTO> getDetalhesVendas() {
        return this.detalhesVendas;
    }

    public void setDetalhesVendas(List<VendaDTO> detalhesVendas) {
        this.detalhesVendas = detalhesVendas;
    }

    public List<ProdutoDTO> getDetalhesProdutos() {
        return this.detalhesProdutos;
    }

    public void setDetalhesProdutos(List<ProdutoDTO> detalhesProdutos) {
        this.detalhesProdutos = detalhesProdutos;
    }

    public List<ClienteResumoDTO> getDetalhesClientes() {
        return this.detalhesClientes;
    }

    public void setDetalhesClientes(List<ClienteResumoDTO> detalhesClientes) {
        this.detalhesClientes = detalhesClientes;
    }

    public List<AtendenteDTO> getDetalhesAtendentes() {
        return this.detalhesAtendentes;
    }

    public void setDetalhesAtendentes(List<AtendenteDTO> detalhesAtendentes) {
        this.detalhesAtendentes = detalhesAtendentes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FiltroRelatorioResponseDTO)) {
            return false;
        }
        FiltroRelatorioResponseDTO that = (FiltroRelatorioResponseDTO)o;
        return Objects.equals(this.relatorioId, that.relatorioId) && Objects.equals(this.dataGeracao, that.dataGeracao) && Objects.equals(this.usuarioSolicitante, that.usuarioSolicitante) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.filtrosAplicados, that.filtrosAplicados) && Objects.equals(this.tempoProcessamentoMs, that.tempoProcessamentoMs) && Objects.equals(this.metricasAggregadas, that.metricasAggregadas) && Objects.equals(this.paginacao, that.paginacao) && Objects.equals(this.ordenacao, that.ordenacao) && Objects.equals(this.detalhesVendas, that.detalhesVendas) && Objects.equals(this.detalhesProdutos, that.detalhesProdutos) && Objects.equals(this.detalhesClientes, that.detalhesClientes) && Objects.equals(this.detalhesAtendentes, that.detalhesAtendentes);
    }

    public int hashCode() {
        return Objects.hash(this.relatorioId, this.dataGeracao, this.usuarioSolicitante, this.tenantId, this.filtrosAplicados, this.tempoProcessamentoMs, this.metricasAggregadas, this.paginacao, this.ordenacao, this.detalhesVendas, this.detalhesProdutos, this.detalhesClientes, this.detalhesAtendentes);
    }

    public String toString() {
        return "FiltroRelatorioResponseDTO{relatorioId=" + String.valueOf(this.relatorioId) + ", dataGeracao=" + String.valueOf(this.dataGeracao) + ", usuarioSolicitante='" + this.usuarioSolicitante + "', tenantId='" + this.tenantId + "', filtrosAplicados=" + String.valueOf(this.filtrosAplicados) + ", tempoProcessamentoMs=" + this.tempoProcessamentoMs + ", metricasAggregadas=" + String.valueOf(this.metricasAggregadas) + ", paginacao=" + String.valueOf(this.paginacao) + ", ordenacao=" + String.valueOf(this.ordenacao) + ", detalhesVendas=" + String.valueOf(this.detalhesVendas) + ", detalhesProdutos=" + String.valueOf(this.detalhesProdutos) + ", detalhesClientes=" + String.valueOf(this.detalhesClientes) + ", detalhesAtendentes=" + String.valueOf(this.detalhesAtendentes) + "}";
    }

    @Schema(name="PaginacaoDTO", description="Dados de pagina\u00e7\u00e3o dos resultados")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class PaginacaoDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="P\u00e1gina atual (zero-based)", example="0")
        @JsonProperty(value="pagina")
        private Integer pagina;
        @Schema(description="Tamanho da p\u00e1gina", example="50")
        @JsonProperty(value="tamanho")
        private Integer tamanho;
        @Schema(description="Total de p\u00e1ginas", example="20")
        @JsonProperty(value="totalPaginas")
        private Integer totalPaginas;
        @Schema(description="Total de registros", example="1000")
        @JsonProperty(value="totalRegistros")
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
            return this.pagina;
        }

        public void setPagina(Integer pagina) {
            this.pagina = pagina;
        }

        public Integer getTamanho() {
            return this.tamanho;
        }

        public void setTamanho(Integer tamanho) {
            this.tamanho = tamanho;
        }

        public Integer getTotalPaginas() {
            return this.totalPaginas;
        }

        public void setTotalPaginas(Integer totalPaginas) {
            this.totalPaginas = totalPaginas;
        }

        public Long getTotalRegistros() {
            return this.totalRegistros;
        }

        public void setTotalRegistros(Long totalRegistros) {
            this.totalRegistros = totalRegistros;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PaginacaoDTO)) {
                return false;
            }
            PaginacaoDTO that = (PaginacaoDTO)o;
            return Objects.equals(this.pagina, that.pagina) && Objects.equals(this.tamanho, that.tamanho) && Objects.equals(this.totalPaginas, that.totalPaginas) && Objects.equals(this.totalRegistros, that.totalRegistros);
        }

        public int hashCode() {
            return Objects.hash(this.pagina, this.tamanho, this.totalPaginas, this.totalRegistros);
        }

        public String toString() {
            return "PaginacaoDTO{pagina=" + this.pagina + ", tamanho=" + this.tamanho + ", totalPaginas=" + this.totalPaginas + ", totalRegistros=" + this.totalRegistros + "}";
        }
    }

    @Schema(name="ProdutoDTO", description="Dados resumidos de cada produto")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class ProdutoDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID do produto", example="789")
        @JsonProperty(value="produtoId")
        private Long produtoId;
        @Schema(description="Nome do produto", example="Dipirona 500mg")
        @JsonProperty(value="nome")
        private String nome;
        @Schema(description="Categoria do produto", example="ANALGESICO")
        @JsonProperty(value="categoria")
        private String categoria;
        @Schema(description="Valor total vendido (no per\u00edodo)", example="1234.56")
        @JsonProperty(value="valorTotalVendido")
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
            return this.produtoId;
        }

        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }

        public String getNome() {
            return this.nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCategoria() {
            return this.categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public BigDecimal getValorTotalVendido() {
            return this.valorTotalVendido;
        }

        public void setValorTotalVendido(BigDecimal valorTotalVendido) {
            this.valorTotalVendido = valorTotalVendido;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ProdutoDTO)) {
                return false;
            }
            ProdutoDTO that = (ProdutoDTO)o;
            return Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.nome, that.nome) && Objects.equals(this.categoria, that.categoria) && Objects.equals(this.valorTotalVendido, that.valorTotalVendido);
        }

        public int hashCode() {
            return Objects.hash(this.produtoId, this.nome, this.categoria, this.valorTotalVendido);
        }

        public String toString() {
            return "ProdutoDTO{produtoId=" + this.produtoId + ", nome='" + this.nome + "', categoria='" + this.categoria + "', valorTotalVendido=" + String.valueOf(this.valorTotalVendido) + "}";
        }
    }

    @Schema(name="VendaDTO", description="Dados resumidos de cada venda")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class VendaDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID da venda", example="456")
        @JsonProperty(value="vendaId")
        private UUID vendaId;
        @Schema(description="Data da venda", type="string", format="date-time", example="2025-07-01T10:00:00")
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty(value="dataVenda")
        private LocalDateTime dataVenda;
        @Schema(description="Valor total da venda", example="200.00")
        @JsonProperty(value="valorVenda")
        private BigDecimal valorVenda;

        public VendaDTO() {
        }

        public VendaDTO(UUID vendaId, LocalDateTime dataVenda, BigDecimal valorVenda) {
            this.vendaId = vendaId;
            this.dataVenda = dataVenda;
            this.valorVenda = valorVenda;
        }

        public UUID getVendaId() {
            return this.vendaId;
        }

        public void setVendaId(UUID vendaId) {
            this.vendaId = vendaId;
        }

        public LocalDateTime getDataVenda() {
            return this.dataVenda;
        }

        public void setDataVenda(LocalDateTime dataVenda) {
            this.dataVenda = dataVenda;
        }

        public BigDecimal getValorVenda() {
            return this.valorVenda;
        }

        public void setValorVenda(BigDecimal valorVenda) {
            this.valorVenda = valorVenda;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof VendaDTO)) {
                return false;
            }
            VendaDTO that = (VendaDTO)o;
            return Objects.equals(this.vendaId, that.vendaId) && Objects.equals(this.dataVenda, that.dataVenda) && Objects.equals(this.valorVenda, that.valorVenda);
        }

        public int hashCode() {
            return Objects.hash(this.vendaId, this.dataVenda, this.valorVenda);
        }

        public String toString() {
            return "VendaDTO{vendaId=" + String.valueOf(this.vendaId) + ", dataVenda=" + String.valueOf(this.dataVenda) + ", valorVenda=" + String.valueOf(this.valorVenda) + "}";
        }
    }
}

