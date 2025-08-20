package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para confirmação de envio de mensagem ao suporte técnico.
 */
@Schema(name = "EmailSuporteResponseDTO", description = "Dados retornados após envio de ticket de suporte")
public class EmailSuporteResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Protocolo do ticket gerado", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.protocolo.notNull}")
    @JsonProperty("protocolo")
    private UUID protocolo;

    @Schema(description = "ID do cliente que abriu o ticket", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.clienteId.notNull}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Status do ticket de suporte", example = "ABERTO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.status.notNull}")
    @JsonProperty("status")
    private StatusTicket status;

    @Schema(description = "Prioridade do ticket", example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.prioridade.notNull}")
    @JsonProperty("prioridade")
    private PrioridadeTicket prioridade;

    @Schema(description = "Canal de origem do ticket", example = "EMAIL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.canalOrigem.notNull}")
    @JsonProperty("canalOrigem")
    private CanalOrigem canalOrigem;

    @Schema(description = "Conteúdo da mensagem enviada", example = "Não estou conseguindo finalizar meu pedido.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{emailSuporteResponse.mensagemRecebida.notBlank}")
    @Size(min = 5, max = 2000, message = "{emailSuporteResponse.mensagemRecebida.size}")
    @JsonProperty("mensagemRecebida")
    private String mensagemRecebida;

    @Schema(description = "Tickets relacionados (escalonamento)")
    @JsonProperty("ticketsRelacionados")
    private List<@NotNull UUID> ticketsRelacionados;

    @Schema(description = "Data/hora de abertura do ticket", type = "string", format = "date-time", example = "2025-07-04T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.dataEnvio.notNull}")
    @PastOrPresent(message = "{emailSuporteResponse.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataEnvio")
    private LocalDateTime dataEnvio;

    @Schema(description = "Prazo de resposta SLA (em horas)", example = "24", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.prazoResposta.notNull}")
    @PositiveOrZero(message = "{emailSuporteResponse.prazoResposta.positiveOrZero}")
    @JsonProperty("prazoRespostaHoras")
    private Integer prazoRespostaHoras;

    @Schema(description = "Deadline de resposta (data/hora)", type = "string", format = "date-time", example = "2025-07-05T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporteResponse.deadline.notNull}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("deadlineResposta")
    private LocalDateTime deadlineResposta;

    @Schema(description = "Usuários/agentes designados ao ticket")
    @JsonProperty("agentesDesignados")
    private List<@NotBlank String> agentesDesignados;

    @Schema(description = "Data/hora da última atualização do ticket", type = "string", format = "date-time", example = "2025-07-04T15:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("ultimaAtualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Schema(description = "Token de correlação (UUID)", example = "5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    // Construtor padrão
    public EmailSuporteResponseDTO() {
    }

    // Construtor completo
    public EmailSuporteResponseDTO(UUID protocolo, Long clienteId, StatusTicket status, PrioridadeTicket prioridade,
            CanalOrigem canalOrigem, String mensagemRecebida, List<UUID> ticketsRelacionados, LocalDateTime dataEnvio,
            Integer prazoRespostaHoras, LocalDateTime deadlineResposta, List<String> agentesDesignados,
            LocalDateTime ultimaAtualizacao, UUID traceId) {
        this.protocolo = protocolo;
        this.clienteId = clienteId;
        this.status = status;
        this.prioridade = prioridade;
        this.canalOrigem = canalOrigem;
        this.mensagemRecebida = mensagemRecebida;
        this.ticketsRelacionados = ticketsRelacionados;
        this.dataEnvio = dataEnvio;
        this.prazoRespostaHoras = prazoRespostaHoras;
        this.deadlineResposta = deadlineResposta;
        this.agentesDesignados = agentesDesignados;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.traceId = traceId;
    }

    // Getters/Setters
    public UUID getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(UUID protocolo) {
        this.protocolo = protocolo;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public StatusTicket getStatus() {
        return status;
    }

    public void setStatus(StatusTicket status) {
        this.status = status;
    }

    public PrioridadeTicket getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeTicket prioridade) {
        this.prioridade = prioridade;
    }

    public CanalOrigem getCanalOrigem() {
        return canalOrigem;
    }

    public void setCanalOrigem(CanalOrigem canalOrigem) {
        this.canalOrigem = canalOrigem;
    }

    public String getMensagemRecebida() {
        return mensagemRecebida;
    }

    public void setMensagemRecebida(String mensagemRecebida) {
        this.mensagemRecebida = mensagemRecebida;
    }

    public List<UUID> getTicketsRelacionados() {
        return ticketsRelacionados;
    }

    public void setTicketsRelacionados(List<UUID> ticketsRelacionados) {
        this.ticketsRelacionados = ticketsRelacionados;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public Integer getPrazoRespostaHoras() {
        return prazoRespostaHoras;
    }

    public void setPrazoRespostaHoras(Integer prazoRespostaHoras) {
        this.prazoRespostaHoras = prazoRespostaHoras;
    }

    public LocalDateTime getDeadlineResposta() {
        return deadlineResposta;
    }

    public void setDeadlineResposta(LocalDateTime deadlineResposta) {
        this.deadlineResposta = deadlineResposta;
    }

    public List<String> getAgentesDesignados() {
        return agentesDesignados;
    }

    public void setAgentesDesignados(List<String> agentesDesignados) {
        this.agentesDesignados = agentesDesignados;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    // utilitários
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EmailSuporteResponseDTO))
            return false;
        EmailSuporteResponseDTO that = (EmailSuporteResponseDTO) o;
        return Objects.equals(protocolo, that.protocolo) && Objects.equals(clienteId, that.clienteId)
                && status == that.status && prioridade == that.prioridade && canalOrigem == that.canalOrigem
                && Objects.equals(mensagemRecebida, that.mensagemRecebida)
                && Objects.equals(ticketsRelacionados, that.ticketsRelacionados)
                && Objects.equals(dataEnvio, that.dataEnvio)
                && Objects.equals(prazoRespostaHoras, that.prazoRespostaHoras)
                && Objects.equals(deadlineResposta, that.deadlineResposta)
                && Objects.equals(agentesDesignados, that.agentesDesignados)
                && Objects.equals(ultimaAtualizacao, that.ultimaAtualizacao) && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolo, clienteId, status, prioridade, canalOrigem, mensagemRecebida,
                ticketsRelacionados, dataEnvio, prazoRespostaHoras, deadlineResposta, agentesDesignados,
                ultimaAtualizacao, traceId);
    }

    @Override
    public String toString() {
        return "EmailSuporteResponseDTO{" + "protocolo=" + protocolo + ", clienteId=" + clienteId + ", status=" + status
                + ", prioridade=" + prioridade + ", canalOrigem=" + canalOrigem + ", mensagemRecebida='"
                + mensagemRecebida + '\'' + ", ticketsRelacionados=" + ticketsRelacionados + ", dataEnvio=" + dataEnvio
                + ", prazoRespostaHoras=" + prazoRespostaHoras + ", deadlineResposta=" + deadlineResposta
                + ", agentesDesignados=" + agentesDesignados + ", ultimaAtualizacao=" + ultimaAtualizacao + ", traceId="
                + traceId + '}';
    }

    // Enums padronizadas (evitam strings soltas)
    public enum StatusTicket {
        ABERTO, EM_ANDAMENTO, FECHADO
    }

    public enum PrioridadeTicket {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum CanalOrigem {
        EMAIL, CHAT, TELEFONE, APP
    }
}
