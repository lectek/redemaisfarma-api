package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de requisição para agendar uso de medicação na API RedeMaisFarma.
 * <p>
 * Permite que o cliente informe qual medicamento usa, com que frequência, em quais horários e se deseja receber
 * lembretes. Inclui metadados de auditoria.
 * </p>
 */
@Schema(name = "AgendamentoMedicacaoRequestDTO", description = "Dados para agendamento de uso de medicação pelo cliente")
public class AgendamentoMedicacaoRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do agendamento (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("agendamentoId")
    private UUID agendamentoId;

    @NotNull(message = "{agendamento.clienteId.notNull}")
    @Schema(description = "ID do cliente que irá usar a medicação", example = "123", required = true)
    @JsonProperty("clienteId")
    private Long clienteId;

    @NotBlank(message = "{agendamento.nomeMedicamento.notBlank}")
    @Size(max = 120, message = "{agendamento.nomeMedicamento.size}")
    @Schema(description = "Nome do medicamento", example = "Losartana 50mg", required = true)
    @JsonProperty("nomeMedicamento")
    private String nomeMedicamento;

    @NotNull(message = "{agendamento.dataInicio.notNull}")
    @FutureOrPresent(message = "{agendamento.dataInicio.futureOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de início do agendamento", example = "2025-07-07", required = true, type = "string", format = "date")
    @JsonProperty("dataInicio")
    private LocalDate dataInicio;

    @NotNull(message = "{agendamento.horario.notNull}")
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Horário diário base para tomar o medicamento", example = "08:00", required = true, type = "string", format = "time")
    @JsonProperty("horario")
    private LocalTime horario;

    @Min(value = 1, message = "{agendamento.frequencia.min}")
    @Max(value = 4, message = "{agendamento.frequencia.max}")
    @Schema(description = "Número de vezes por dia que o medicamento será tomado (1 a 4)", example = "2", required = true)
    @JsonProperty("frequencia")
    private int frequencia;

    @Size(max = 500, message = "{agendamento.observacoes.size}")
    @Schema(description = "Observações do paciente ou do farmacêutico", example = "Tomar com água. Jejum obrigatório.")
    @JsonProperty("observacoes")
    private String observacoes;

    @Schema(description = "Se deve enviar lembretes de notificação para o cliente", example = "true", required = true)
    @JsonProperty("enviarNotificacoes")
    private boolean enviarNotificacoes;

    // Metadados padrão do projeto
    @NotBlank(message = "{agendamento.tenantId.notBlank}")
    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID) para auditoria", example = "4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @PastOrPresent(message = "{agendamento.criadoEm.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data/hora de criação do registro", example = "2025-07-07T09:30:00", type = "string", format = "date-time")
    @JsonProperty("criadoEm")
    private LocalDateTime criadoEm;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data/hora da última atualização do registro", example = "2025-07-08T10:00:00", type = "string", format = "date-time")
    @JsonProperty("atualizadoEm")
    private LocalDateTime atualizadoEm;

    public AgendamentoMedicacaoRequestDTO() {
    }

    public AgendamentoMedicacaoRequestDTO(UUID agendamentoId, Long clienteId, String nomeMedicamento,
            LocalDate dataInicio, LocalTime horario, int frequencia, String observacoes, boolean enviarNotificacoes,
            String tenantId, UUID traceId, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.agendamentoId = agendamentoId;
        this.clienteId = clienteId;
        this.nomeMedicamento = nomeMedicamento;
        this.dataInicio = dataInicio;
        this.horario = horario;
        this.frequencia = frequencia;
        this.observacoes = observacoes;
        this.enviarNotificacoes = enviarNotificacoes;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    // Getters/Setters
    public UUID getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(UUID agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeMedicamento() {
        return nomeMedicamento;
    }

    public void setNomeMedicamento(String nomeMedicamento) {
        this.nomeMedicamento = nomeMedicamento;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isEnviarNotificacoes() {
        return enviarNotificacoes;
    }

    public void setEnviarNotificacoes(boolean enviarNotificacoes) {
        this.enviarNotificacoes = enviarNotificacoes;
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

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    // equals/hashCode/toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AgendamentoMedicacaoRequestDTO))
            return false;
        AgendamentoMedicacaoRequestDTO that = (AgendamentoMedicacaoRequestDTO) o;
        return frequencia == that.frequencia && enviarNotificacoes == that.enviarNotificacoes
                && Objects.equals(agendamentoId, that.agendamentoId) && Objects.equals(clienteId, that.clienteId)
                && Objects.equals(nomeMedicamento, that.nomeMedicamento) && Objects.equals(dataInicio, that.dataInicio)
                && Objects.equals(horario, that.horario) && Objects.equals(observacoes, that.observacoes)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(criadoEm, that.criadoEm) && Objects.equals(atualizadoEm, that.atualizadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agendamentoId, clienteId, nomeMedicamento, dataInicio, horario, frequencia, observacoes,
                enviarNotificacoes, tenantId, traceId, criadoEm, atualizadoEm);
    }

    @Override
    public String toString() {
        return "AgendamentoMedicacaoRequestDTO{" + "agendamentoId=" + agendamentoId + ", clienteId=" + clienteId
                + ", nomeMedicamento='" + nomeMedicamento + '\'' + ", dataInicio=" + dataInicio + ", horario=" + horario
                + ", frequencia=" + frequencia + ", observacoes='" + observacoes + '\'' + ", enviarNotificacoes="
                + enviarNotificacoes + ", tenantId='" + tenantId + '\'' + ", traceId=" + traceId + ", criadoEm="
                + criadoEm + ", atualizadoEm=" + atualizadoEm + '}';
    }
}
