package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para listagem de pedidos na API RedeMaisFarma.
 */
@Schema(name = "ListaPedidoResponse", description = "Resposta contendo lista de pedidos e metadados")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListaPedidoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Lista de pedidos conforme consulta.
     */
    @Schema(description = "Lista de pedidos retornados")
    @NotNull(message = "{listaPedido.pedidos.notNull}")
    @Valid
    @JsonProperty("pedidos")
    private List<PedidoResponseDTO> pedidos;

    /**
     * Informações de paginação (página atual, tamanho, total de páginas, total de registros).
     */
    @Schema(description = "Dados de paginação dos resultados")
    @NotNull(message = "{listaPedido.paginacao.notNull}")
    @Valid
    @JsonProperty("paginacao")
    private PaginacaoDTO paginacao;

    /**
     * Critérios de ordenação aplicados (campo,ASC|DESC).
     */
    @Schema(description = "Critérios de ordenação usados na consulta", example = "[\"dataPedido,DESC\"]")
    @JsonProperty("ordenacao")
    private List<@Pattern(regexp = "[a-zA-Z0-9_]+,(ASC|DESC)", message = "{listaPedido.ordenacao.pattern}") String> ordenacao;

    /**
     * Filtros aplicados na consulta (campo -> valor).
     */
    @Schema(description = "Filtros aplicados na geração da lista")
    @JsonProperty("filtrosAplicados")
    private Map<@NotBlank String, @NotBlank String> filtrosAplicados;

    /**
     * Status da consulta (e.g., SUCCESS, EMPTY, ERROR).
     */
    @Schema(description = "Status da consulta de pedidos", example = "SUCCESS", required = true)
    @NotBlank(message = "{listaPedido.status.notBlank}")
    @JsonProperty("statusConsulta")
    private String statusConsulta;

    /**
     * Timestamp de geração da resposta.
     */
    @Schema(description = "Data/hora de geração da resposta", type = "string", format = "date-time", example = "2025-07-04T17:00:00", required = true)
    @NotNull(message = "{listaPedido.geradoEm.notNull}")
    @PastOrPresent(message = "{listaPedido.geradoEm.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("geradoEm")
    private LocalDateTime geradoEm;

    /**
     * Tempo de processamento da consulta em milissegundos.
     */
    @Schema(description = "Tempo de processamento da consulta (ms)", example = "150")
    @PositiveOrZero(message = "{listaPedido.tempoProcessamento.min}")
    @JsonProperty("tempoProcessamentoMs")
    private Long tempoProcessamentoMs;

    /**
     * Usuário que solicitou a lista.
     */
    @Schema(description = "Usuário que solicitou a lista", example = "joao.silva", required = true)
    @NotBlank(message = "{listaPedido.usuarioSolicitante.notBlank}")
    @JsonProperty("usuarioSolicitante")
    private String usuarioSolicitante;

    /**
     * Identificador do tenant (multi-inquilino) para auditoria.
     */
    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{listaPedido.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    /**
     * Token de rastreamento para correlação de logs distribuídos.
     */
    @Schema(description = "Token de correlação (UUID)", example = "5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------

    public ListaPedidoResponse() {
    }

    public ListaPedidoResponse(List<PedidoResponseDTO> pedidos, PaginacaoDTO paginacao, List<String> ordenacao,
            Map<String, String> filtrosAplicados, String statusConsulta, LocalDateTime geradoEm,
            Long tempoProcessamentoMs, String usuarioSolicitante, String tenantId, UUID traceId) {
        this.pedidos = pedidos;
        this.paginacao = paginacao;
        this.ordenacao = ordenacao;
        this.filtrosAplicados = filtrosAplicados;
        this.statusConsulta = statusConsulta;
        this.geradoEm = geradoEm;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
        this.usuarioSolicitante = usuarioSolicitante;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    // ----------------------------------------------------------------------
    // Getters e Setters
    // ----------------------------------------------------------------------

    public List<PedidoResponseDTO> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<PedidoResponseDTO> pedidos) {
        this.pedidos = pedidos;
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

    public Map<String, String> getFiltrosAplicados() {
        return filtrosAplicados;
    }

    public void setFiltrosAplicados(Map<String, String> filtrosAplicados) {
        this.filtrosAplicados = filtrosAplicados;
    }

    public String getStatusConsulta() {
        return statusConsulta;
    }

    public void setStatusConsulta(String statusConsulta) {
        this.statusConsulta = statusConsulta;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(LocalDateTime geradoEm) {
        this.geradoEm = geradoEm;
    }

    public Long getTempoProcessamentoMs() {
        return tempoProcessamentoMs;
    }

    public void setTempoProcessamentoMs(Long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
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

    // ----------------------------------------------------------------------
    // Métodos utilitários
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ListaPedidoResponse))
            return false;
        ListaPedidoResponse that = (ListaPedidoResponse) o;
        return Objects.equals(pedidos, that.pedidos) && Objects.equals(paginacao, that.paginacao)
                && Objects.equals(ordenacao, that.ordenacao) && Objects.equals(filtrosAplicados, that.filtrosAplicados)
                && Objects.equals(statusConsulta, that.statusConsulta) && Objects.equals(geradoEm, that.geradoEm)
                && Objects.equals(tempoProcessamentoMs, that.tempoProcessamentoMs)
                && Objects.equals(usuarioSolicitante, that.usuarioSolicitante)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedidos, paginacao, ordenacao, filtrosAplicados, statusConsulta, geradoEm,
                tempoProcessamentoMs, usuarioSolicitante, tenantId, traceId);
    }

    @Override
    public String toString() {
        return "ListaPedidoResponse{" + "pedidos=" + pedidos + ", paginacao=" + paginacao + ", ordenacao=" + ordenacao
                + ", filtrosAplicados=" + filtrosAplicados + ", statusConsulta='" + statusConsulta + '\''
                + ", geradoEm=" + geradoEm + ", tempoProcessamentoMs=" + tempoProcessamentoMs + ", usuarioSolicitante='"
                + usuarioSolicitante + '\'' + ", tenantId='" + tenantId + '\'' + ", traceId=" + traceId + '}';
    }

    // ----------------------------------------------------------------------
    // DTO interno: Paginação
    // ----------------------------------------------------------------------
    @Schema(name = "PaginacaoDTO", description = "Dados de paginação")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginacaoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Página atual (zero-based)", example = "0", required = true)
        @NotNull(message = "{paginacao.pagina.notNull}")
        @Min(value = 0, message = "{paginacao.pagina.min}")
        @JsonProperty("paginaAtual")
        private Integer paginaAtual;

        @Schema(description = "Tamanho da página", example = "50", required = true)
        @NotNull(message = "{paginacao.tamanho.notNull}")
        @Min(value = 1, message = "{paginacao.tamanho.min}")
        @JsonProperty("tamanhoPagina")
        private Integer tamanhoPagina;

        @Schema(description = "Total de páginas", example = "20", required = true)
        @NotNull(message = "{paginacao.totalPaginas.notNull}")
        @Min(value = 0, message = "{paginacao.totalPaginas.min}")
        @JsonProperty("totalPaginas")
        private Integer totalPaginas;

        @Schema(description = "Total de registros encontrados", example = "1000", required = true)
        @NotNull(message = "{paginacao.totalRegistros.notNull}")
        @Min(value = 0, message = "{paginacao.totalRegistros.min}")
        @JsonProperty("totalRegistros")
        private Long totalRegistros;

        public PaginacaoDTO() {
        }

        public PaginacaoDTO(Integer paginaAtual, Integer tamanhoPagina, Integer totalPaginas, Long totalRegistros) {
            this.paginaAtual = paginaAtual;
            this.tamanhoPagina = tamanhoPagina;
            this.totalPaginas = totalPaginas;
            this.totalRegistros = totalRegistros;
        }

        public Integer getPaginaAtual() {
            return paginaAtual;
        }

        public void setPaginaAtual(Integer paginaAtual) {
            this.paginaAtual = paginaAtual;
        }

        public Integer getTamanhoPagina() {
            return tamanhoPagina;
        }

        public void setTamanhoPagina(Integer tamanhoPagina) {
            this.tamanhoPagina = tamanhoPagina;
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
            if (!(o instanceof PaginacaoDTO))
                return false;
            PaginacaoDTO that = (PaginacaoDTO) o;
            return Objects.equals(paginaAtual, that.paginaAtual) && Objects.equals(tamanhoPagina, that.tamanhoPagina)
                    && Objects.equals(totalPaginas, that.totalPaginas)
                    && Objects.equals(totalRegistros, that.totalRegistros);
        }

        @Override
        public int hashCode() {
            return Objects.hash(paginaAtual, tamanhoPagina, totalPaginas, totalRegistros);
        }

        @Override
        public String toString() {
            return "PaginacaoDTO{" + "paginaAtual=" + paginaAtual + ", tamanhoPagina=" + tamanhoPagina
                    + ", totalPaginas=" + totalPaginas + ", totalRegistros=" + totalRegistros + '}';
        }
    }
}
