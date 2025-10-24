/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonFormat$Shape
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$RequiredMode
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.PositiveOrZero
 */
package br.com.redemaisfarma.application.dto.response;

import br.com.redemaisfarma.application.dto.response.PedidoResponseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Schema(name="ListaPedidoResponse", description="Resposta contendo lista de pedidos e metadados")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ListaPedidoResponse
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="Lista de pedidos retornados", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{listaPedido.pedidos.notNull}")
    @Valid
    @JsonProperty(value="pedidos")
    private @NotNull(message="{listaPedido.pedidos.notNull}") @Valid List<PedidoResponseDTO> pedidos;
    @Schema(description="Dados de pagina\u00e7\u00e3o dos resultados", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{listaPedido.paginacao.notNull}")
    @Valid
    @JsonProperty(value="paginacao")
    private @NotNull(message="{listaPedido.paginacao.notNull}") @Valid PaginacaoDTO paginacao;
    @Schema(description="Crit\u00e9rios de ordena\u00e7\u00e3o usados na consulta", example="[\"dataPedido,DESC\"]")
    @JsonProperty(value="ordenacao")
    private List<@Pattern(regexp="[a-zA-Z0-9_]+,(ASC|DESC)", message="{listaPedido.ordenacao.pattern}") String> ordenacao;
    @Schema(description="Filtros aplicados na gera\u00e7\u00e3o da lista")
    @JsonProperty(value="filtrosAplicados")
    private Map<@NotBlank String, @NotBlank String> filtrosAplicados;
    @Schema(description="Status da consulta de pedidos", example="SUCCESS", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{listaPedido.status.notBlank}")
    @JsonProperty(value="statusConsulta")
    private @NotBlank(message="{listaPedido.status.notBlank}") String statusConsulta;
    @Schema(description="Data/hora de gera\u00e7\u00e3o da resposta", type="string", format="date-time", example="2025-07-04T17:00:00", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{listaPedido.geradoEm.notNull}")
    @PastOrPresent(message="{listaPedido.geradoEm.pastOrPresent}")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="geradoEm")
    private @NotNull(message="{listaPedido.geradoEm.notNull}") @PastOrPresent(message="{listaPedido.geradoEm.pastOrPresent}") LocalDateTime geradoEm;
    @Schema(description="Tempo de processamento da consulta (ms)", example="150")
    @PositiveOrZero(message="{listaPedido.tempoProcessamento.min}")
    @JsonProperty(value="tempoProcessamentoMs")
    private @PositiveOrZero(message="{listaPedido.tempoProcessamento.min}") Long tempoProcessamentoMs;
    @Schema(description="Usu\u00e1rio que solicitou a lista", example="joao.silva", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{listaPedido.usuarioSolicitante.notBlank}")
    @JsonProperty(value="usuarioSolicitante")
    private @NotBlank(message="{listaPedido.usuarioSolicitante.notBlank}") String usuarioSolicitante;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{listaPedido.tenantId.notBlank}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{listaPedido.tenantId.notBlank}") String tenantId;
    @Schema(description="Token de correla\u00e7\u00e3o (UUID)", example="5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;

    public ListaPedidoResponse() {
    }

    public ListaPedidoResponse(List<PedidoResponseDTO> pedidos, PaginacaoDTO paginacao, List<String> ordenacao, Map<String, String> filtrosAplicados, String statusConsulta, LocalDateTime geradoEm, Long tempoProcessamentoMs, String usuarioSolicitante, String tenantId, UUID traceId) {
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

    public List<PedidoResponseDTO> getPedidos() {
        return this.pedidos;
    }

    public void setPedidos(List<PedidoResponseDTO> pedidos) {
        this.pedidos = pedidos;
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

    public Map<String, String> getFiltrosAplicados() {
        return this.filtrosAplicados;
    }

    public void setFiltrosAplicados(Map<String, String> filtrosAplicados) {
        this.filtrosAplicados = filtrosAplicados;
    }

    public String getStatusConsulta() {
        return this.statusConsulta;
    }

    public void setStatusConsulta(String statusConsulta) {
        this.statusConsulta = statusConsulta;
    }

    public LocalDateTime getGeradoEm() {
        return this.geradoEm;
    }

    public void setGeradoEm(LocalDateTime geradoEm) {
        this.geradoEm = geradoEm;
    }

    public Long getTempoProcessamentoMs() {
        return this.tempoProcessamentoMs;
    }

    public void setTempoProcessamentoMs(Long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
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

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ListaPedidoResponse)) {
            return false;
        }
        ListaPedidoResponse that = (ListaPedidoResponse)o;
        return Objects.equals(this.pedidos, that.pedidos) && Objects.equals(this.paginacao, that.paginacao) && Objects.equals(this.ordenacao, that.ordenacao) && Objects.equals(this.filtrosAplicados, that.filtrosAplicados) && Objects.equals(this.statusConsulta, that.statusConsulta) && Objects.equals(this.geradoEm, that.geradoEm) && Objects.equals(this.tempoProcessamentoMs, that.tempoProcessamentoMs) && Objects.equals(this.usuarioSolicitante, that.usuarioSolicitante) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId);
    }

    public int hashCode() {
        return Objects.hash(this.pedidos, this.paginacao, this.ordenacao, this.filtrosAplicados, this.statusConsulta, this.geradoEm, this.tempoProcessamentoMs, this.usuarioSolicitante, this.tenantId, this.traceId);
    }

    public String toString() {
        return "ListaPedidoResponse{pedidos=" + String.valueOf(this.pedidos) + ", paginacao=" + String.valueOf(this.paginacao) + ", ordenacao=" + String.valueOf(this.ordenacao) + ", filtrosAplicados=" + String.valueOf(this.filtrosAplicados) + ", statusConsulta='" + this.statusConsulta + "', geradoEm=" + String.valueOf(this.geradoEm) + ", tempoProcessamentoMs=" + this.tempoProcessamentoMs + ", usuarioSolicitante='" + this.usuarioSolicitante + "', tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + "}";
    }

    @Schema(name="PaginacaoDTO", description="Dados de pagina\u00e7\u00e3o")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class PaginacaoDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="P\u00e1gina atual (zero-based)", example="0", requiredMode=Schema.RequiredMode.REQUIRED)
        @NotNull(message="{paginacao.pagina.notNull}")
        @Min(value=0L, message="{paginacao.pagina.min}")
        @JsonProperty(value="paginaAtual")
        private @NotNull(message="{paginacao.pagina.notNull}") @Min(value=0L, message="{paginacao.pagina.min}") Integer paginaAtual;
        @Schema(description="Tamanho da p\u00e1gina", example="50", requiredMode=Schema.RequiredMode.REQUIRED)
        @NotNull(message="{paginacao.tamanho.notNull}")
        @Min(value=1L, message="{paginacao.tamanho.min}")
        @JsonProperty(value="tamanhoPagina")
        private @NotNull(message="{paginacao.tamanho.notNull}") @Min(value=1L, message="{paginacao.tamanho.min}") Integer tamanhoPagina;
        @Schema(description="Total de p\u00e1ginas", example="20", requiredMode=Schema.RequiredMode.REQUIRED)
        @NotNull(message="{paginacao.totalPaginas.notNull}")
        @Min(value=0L, message="{paginacao.totalPaginas.min}")
        @JsonProperty(value="totalPaginas")
        private @NotNull(message="{paginacao.totalPaginas.notNull}") @Min(value=0L, message="{paginacao.totalPaginas.min}") Integer totalPaginas;
        @Schema(description="Total de registros encontrados", example="1000", requiredMode=Schema.RequiredMode.REQUIRED)
        @NotNull(message="{paginacao.totalRegistros.notNull}")
        @Min(value=0L, message="{paginacao.totalRegistros.min}")
        @JsonProperty(value="totalRegistros")
        private @NotNull(message="{paginacao.totalRegistros.notNull}") @Min(value=0L, message="{paginacao.totalRegistros.min}") Long totalRegistros;

        public PaginacaoDTO() {
        }

        public PaginacaoDTO(Integer paginaAtual, Integer tamanhoPagina, Integer totalPaginas, Long totalRegistros) {
            this.paginaAtual = paginaAtual;
            this.tamanhoPagina = tamanhoPagina;
            this.totalPaginas = totalPaginas;
            this.totalRegistros = totalRegistros;
        }

        public Integer getPaginaAtual() {
            return this.paginaAtual;
        }

        public void setPaginaAtual(Integer paginaAtual) {
            this.paginaAtual = paginaAtual;
        }

        public Integer getTamanhoPagina() {
            return this.tamanhoPagina;
        }

        public void setTamanhoPagina(Integer tamanhoPagina) {
            this.tamanhoPagina = tamanhoPagina;
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
            return Objects.equals(this.paginaAtual, that.paginaAtual) && Objects.equals(this.tamanhoPagina, that.tamanhoPagina) && Objects.equals(this.totalPaginas, that.totalPaginas) && Objects.equals(this.totalRegistros, that.totalRegistros);
        }

        public int hashCode() {
            return Objects.hash(this.paginaAtual, this.tamanhoPagina, this.totalPaginas, this.totalRegistros);
        }

        public String toString() {
            return "PaginacaoDTO{paginaAtual=" + this.paginaAtual + ", tamanhoPagina=" + this.tamanhoPagina + ", totalPaginas=" + this.totalPaginas + ", totalRegistros=" + this.totalRegistros + "}";
        }
    }
}

