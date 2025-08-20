package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para interações com Assistente Virtual (IA) na API RedeMaisFarma.
 *
 * Contém informações da pergunta do usuário, resposta da IA, metadados de consumo, rastreamento e histórico de
 * conversas.
 */
@Schema(name = "MensagemIAResponseDTO", description = "Dados retornados de interação com IA RedeMaisFarma")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MensagemIAResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Identificadores e auditoria
    // ----------------------------------------------------------------------

    @Schema(description = "ID da interação com IA", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    @NotNull(message = "{mensagemIAResponse.interacaoId.notNull}")
    @JsonProperty("interacaoId")
    private UUID interacaoId;

    @Schema(description = "ID do cliente", example = "12345", required = true)
    @NotNull(message = "{mensagemIAResponse.clienteId.notNull}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Token de correlação (UUID)", example = "4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{mensagemIAResponse.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Data/hora da interação", type = "string", format = "date-time", example = "2025-07-04T15:00:00", required = true)
    @NotNull(message = "{mensagemIAResponse.dataHoraInteracao.notNull}")
    @PastOrPresent(message = "{mensagemIAResponse.dataHoraInteracao.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataHoraInteracao")
    private LocalDateTime dataHoraInteracao;

    @Schema(description = "Data/hora da última atualização", type = "string", format = "date-time", example = "2025-07-04T15:05:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("atualizadoEm")
    private LocalDateTime atualizadoEm;

    // ----------------------------------------------------------------------
    // Conteúdo da interação
    // ----------------------------------------------------------------------

    @Schema(description = "Pergunta enviada pelo usuário", example = "Qual a dosagem recomendada para dor de cabeça?", required = true)
    @NotBlank(message = "{mensagemIAResponse.perguntaUsuario.notBlank}")
    @Size(max = 2000, message = "{mensagemIAResponse.perguntaUsuario.size}")
    @JsonProperty("perguntaUsuario")
    private String perguntaUsuario;

    @Schema(description = "Resposta fornecida pela IA", example = "Recomenda-se 500mg a cada 6 horas.", required = true)
    @NotBlank(message = "{mensagemIAResponse.respostaIA.notBlank}")
    @Size(max = 4000, message = "{mensagemIAResponse.respostaIA.size}")
    @JsonProperty("respostaIA")
    private String respostaIA;

    @Schema(description = "Tipo de resposta da IA", example = "EXPLICATIVA", required = true, allowableValues = {
            "EXPLICATIVA", "CONCISA", "PASSO_A_PASSO" })
    @NotNull(message = "{mensagemIAResponse.tipoResposta.notNull}")
    @JsonProperty("tipoResposta")
    private TipoResposta tipoResposta;

    @Schema(description = "Explicação ou esclarecimento adicional", example = "Evite uso excessivo...")
    @Size(max = 2000, message = "{mensagemIAResponse.explicacaoSugerida.size}")
    @JsonProperty("explicacaoSugerida")
    private String explicacaoSugerida;

    @Schema(description = "Plano/modelo de IA usado", example = "GPT-4", required = true)
    @NotBlank(message = "{mensagemIAResponse.planoIA.notBlank}")
    @Size(max = 50, message = "{mensagemIAResponse.planoIA.size}")
    @JsonProperty("planoIA")
    private String planoIA;

    // ----------------------------------------------------------------------
    // Consumo de tokens e limites
    // ----------------------------------------------------------------------

    @Schema(description = "Quantidade de tokens consumidos na resposta IA", example = "150")
    @PositiveOrZero(message = "{mensagemIAResponse.consumoAtual.positiveOrZero}")
    @JsonProperty("consumoAtual")
    private Integer consumoAtual;

    @Schema(description = "Limite mensal de tokens do plano IA", example = "1000000")
    @Positive(message = "{mensagemIAResponse.limiteMensal.positive}")
    @JsonProperty("limiteMensal")
    private Integer limiteMensal;

    @Schema(description = "Canal de origem da interação IA", example = "APP", required = true, allowableValues = {
            "WEB", "APP", "ADMIN" })
    @NotNull(message = "{mensagemIAResponse.canalOrigem.notNull}")
    @JsonProperty("canalOrigem")
    private CanalOrigem canalOrigem;

    // ----------------------------------------------------------------------
    // Histórico de interações
    // ----------------------------------------------------------------------

    @Schema(description = "Contexto de interações anteriores")
    @Valid
    @JsonProperty("historicoInteracoes")
    private List<HistoricoInteracaoDTO> historicoInteracoes;

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------

    public MensagemIAResponseDTO() {
    }

    public MensagemIAResponseDTO(UUID interacaoId, Long clienteId, UUID traceId, String tenantId,
            LocalDateTime dataHoraInteracao, LocalDateTime atualizadoEm, String perguntaUsuario, String respostaIA,
            TipoResposta tipoResposta, String explicacaoSugerida, String planoIA, Integer consumoAtual,
            Integer limiteMensal, CanalOrigem canalOrigem, List<HistoricoInteracaoDTO> historicoInteracoes) {
        this.interacaoId = interacaoId;
        this.clienteId = clienteId;
        this.traceId = traceId;
        this.tenantId = tenantId;
        this.dataHoraInteracao = dataHoraInteracao;
        this.atualizadoEm = atualizadoEm;
        this.perguntaUsuario = perguntaUsuario;
        this.respostaIA = respostaIA;
        this.tipoResposta = tipoResposta;
        this.explicacaoSugerida = explicacaoSugerida;
        this.planoIA = planoIA;
        this.consumoAtual = consumoAtual;
        this.limiteMensal = limiteMensal;
        this.canalOrigem = canalOrigem;
        this.historicoInteracoes = historicoInteracoes;
    }

    // ----------------------------------------------------------------------
    // Getters e Setters
    // ----------------------------------------------------------------------

    public UUID getInteracaoId() {
        return interacaoId;
    }

    public void setInteracaoId(UUID interacaoId) {
        this.interacaoId = interacaoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getDataHoraInteracao() {
        return dataHoraInteracao;
    }

    public void setDataHoraInteracao(LocalDateTime dataHoraInteracao) {
        this.dataHoraInteracao = dataHoraInteracao;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getPerguntaUsuario() {
        return perguntaUsuario;
    }

    public void setPerguntaUsuario(String perguntaUsuario) {
        this.perguntaUsuario = perguntaUsuario;
    }

    public String getRespostasIA() {
        return respostaIA;
    } // compat: nome alternativo se necessário

    public String getRespostaIA() {
        return respostaIA;
    }

    public void setRespostaIA(String respostaIA) {
        this.respostaIA = respostaIA;
    }

    public TipoResposta getTipoResposta() {
        return tipoResposta;
    }

    public void setTipoResposta(TipoResposta tipoResposta) {
        this.tipoResposta = tipoResposta;
    }

    public String getExplicacaoSugerida() {
        return explicacaoSugerida;
    }

    public void setExplicacaoSugerida(String explicacaoSugerida) {
        this.explicacaoSugerida = explicacaoSugerida;
    }

    public String getPlanoIA() {
        return planoIA;
    }

    public void setPlanoIA(String planoIA) {
        this.planoIA = planoIA;
    }

    public Integer getConsumoAtual() {
        return consumoAtual;
    }

    public void setConsumoAtual(Integer consumoAtual) {
        this.consumoAtual = consumoAtual;
    }

    public Integer getLimiteMensal() {
        return limiteMensal;
    }

    public void setLimiteMensal(Integer limiteMensal) {
        this.limiteMensal = limiteMensal;
    }

    public CanalOrigem getCanalOrigem() {
        return canalOrigem;
    }

    public void setCanalOrigem(CanalOrigem canalOrigem) {
        this.canalOrigem = canalOrigem;
    }

    public List<HistoricoInteracaoDTO> getHistoricoInteracoes() {
        return historicoInteracoes;
    }

    public void setHistoricoInteracoes(List<HistoricoInteracaoDTO> historicoInteracoes) {
        this.historicoInteracoes = historicoInteracoes;
    }

    // ----------------------------------------------------------------------
    // Métodos utilitários
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MensagemIAResponseDTO))
            return false;
        MensagemIAResponseDTO that = (MensagemIAResponseDTO) o;
        return Objects.equals(interacaoId, that.interacaoId) && Objects.equals(clienteId, that.clienteId)
                && Objects.equals(traceId, that.traceId) && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(dataHoraInteracao, that.dataHoraInteracao)
                && Objects.equals(atualizadoEm, that.atualizadoEm)
                && Objects.equals(perguntaUsuario, that.perguntaUsuario) && Objects.equals(respostaIA, that.respostaIA)
                && tipoResposta == that.tipoResposta && Objects.equals(explicacaoSugerida, that.explicacaoSugerida)
                && Objects.equals(planoIA, that.planoIA) && Objects.equals(consumoAtual, that.consumoAtual)
                && Objects.equals(limiteMensal, that.limiteMensal) && canalOrigem == that.canalOrigem
                && Objects.equals(historicoInteracoes, that.historicoInteracoes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interacaoId, clienteId, traceId, tenantId, dataHoraInteracao, atualizadoEm, perguntaUsuario,
                respostaIA, tipoResposta, explicacaoSugerida, planoIA, consumoAtual, limiteMensal, canalOrigem,
                historicoInteracoes);
    }

    @Override
    public String toString() {
        return "MensagemIAResponseDTO{" + "interacaoId=" + interacaoId + ", clienteId=" + clienteId + ", traceId="
                + traceId + ", tenantId='" + tenantId + '\'' + ", dataHoraInteracao=" + dataHoraInteracao
                + ", atualizadoEm=" + atualizadoEm + ", perguntaUsuario='" + perguntaUsuario + '\'' + ", respostaIA='"
                + respostaIA + '\'' + ", tipoResposta=" + tipoResposta + ", explicacaoSugerida='" + explicacaoSugerida
                + '\'' + ", planoIA='" + planoIA + '\'' + ", consumoAtual=" + consumoAtual + ", limiteMensal="
                + limiteMensal + ", canalOrigem=" + canalOrigem + ", historicoInteracoes=" + historicoInteracoes + '}';
    }

    // ----------------------------------------------------------------------
    // Enums
    // ----------------------------------------------------------------------

    @Schema(enumAsRef = true, description = "Tipo de resposta gerada pela IA")
    public enum TipoResposta {
        EXPLICATIVA, CONCISA, PASSO_A_PASSO
    }

    @Schema(enumAsRef = true, description = "Canal de origem da interação")
    public enum CanalOrigem {
        WEB, APP, ADMIN
    }

    // ----------------------------------------------------------------------
    // DTO interno
    // ----------------------------------------------------------------------

    @Schema(name = "HistoricoInteracaoDTO", description = "Registro de interações anteriores")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HistoricoInteracaoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID único do registro", example = "7fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
        @NotNull(message = "{historicoInteracao.registroId.notNull}")
        @JsonProperty("registroId")
        private UUID registroId;

        @Schema(description = "Pergunta do usuário", example = "Qual horário de funcionamento?", required = true)
        @NotBlank(message = "{historicoInteracao.pergunta.notBlank}")
        @Size(max = 2000, message = "{historicoInteracao.pergunta.size}")
        @JsonProperty("pergunta")
        private String pergunta;

        @Schema(description = "Resposta da IA", example = "Funcionamos 24h.", required = true)
        @NotBlank(message = "{historicoInteracao.resposta.notBlank}")
        @Size(max = 4000, message = "{historicoInteracao.resposta.size}")
        @JsonProperty("resposta")
        private String resposta;

        @Schema(description = "Timestamp do registro", type = "string", format = "date-time", example = "2025-07-04T14:59:00", required = true)
        @NotNull(message = "{historicoInteracao.dataHora.notNull}")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("dataHora")
        private LocalDateTime dataHora;

        public HistoricoInteracaoDTO() {
        }

        public HistoricoInteracaoDTO(UUID registroId, String pergunta, String resposta, LocalDateTime dataHora) {
            this.registroId = registroId;
            this.pergunta = pergunta;
            this.resposta = resposta;
            this.dataHora = dataHora;
        }

        public UUID getRegistroId() {
            return registroId;
        }

        public void setRegistroId(UUID registroId) {
            this.registroId = registroId;
        }

        public String getPergunta() {
            return pergunta;
        }

        public void setPergunta(String pergunta) {
            this.pergunta = pergunta;
        }

        public String getResposta() {
            return resposta;
        }

        public void setResposta(String resposta) {
            this.resposta = resposta;
        }

        public LocalDateTime getDataHora() {
            return dataHora;
        }

        public void setDataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof HistoricoInteracaoDTO))
                return false;
            HistoricoInteracaoDTO that = (HistoricoInteracaoDTO) o;
            return Objects.equals(registroId, that.registroId) && Objects.equals(pergunta, that.pergunta)
                    && Objects.equals(resposta, that.resposta) && Objects.equals(dataHora, that.dataHora);
        }

        @Override
        public int hashCode() {
            return Objects.hash(registroId, pergunta, resposta, dataHora);
        }

        @Override
        public String toString() {
            return "HistoricoInteracaoDTO{" + "registroId=" + registroId + ", pergunta='" + pergunta + '\''
                    + ", resposta='" + resposta + '\'' + ", dataHora=" + dataHora + '}';
        }
    }
}
