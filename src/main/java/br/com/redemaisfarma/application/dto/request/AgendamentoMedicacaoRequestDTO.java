/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.constraints.FutureOrPresent
 *  jakarta.validation.constraints.Max
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Schema(name="AgendamentoMedicacaoRequestDTO", description="Dados para agendamento de uso de medica\u00e7\u00e3o pelo cliente")
public class AgendamentoMedicacaoRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do agendamento (UUID)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="agendamentoId")
    private UUID agendamentoId;
    @NotNull(message="{agendamento.clienteId.notNull}")
    @Schema(description="ID do cliente que ir\u00e1 usar a medica\u00e7\u00e3o", example="123", required=true)
    @JsonProperty(value="clienteId")
    private @NotNull(message="{agendamento.clienteId.notNull}") Long clienteId;
    @NotBlank(message="{agendamento.nomeMedicamento.notBlank}")
    @Size(max=120, message="{agendamento.nomeMedicamento.size}")
    @Schema(description="Nome do medicamento", example="Losartana 50mg", required=true)
    @JsonProperty(value="nomeMedicamento")
    private @NotBlank(message="{agendamento.nomeMedicamento.notBlank}") @Size(max=120, message="{agendamento.nomeMedicamento.size}") String nomeMedicamento;
    @NotNull(message="{agendamento.dataInicio.notNull}")
    @FutureOrPresent(message="{agendamento.dataInicio.futureOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd")
    @Schema(description="Data de in\u00edcio do agendamento", example="2025-07-07", required=true, type="string", format="date")
    @JsonProperty(value="dataInicio")
    private @NotNull(message="{agendamento.dataInicio.notNull}") @FutureOrPresent(message="{agendamento.dataInicio.futureOrPresent}") LocalDate dataInicio;
    @NotNull(message="{agendamento.horario.notNull}")
    @JsonFormat(pattern="HH:mm")
    @Schema(description="Hor\u00e1rio di\u00e1rio base para tomar o medicamento", example="08:00", required=true, type="string", format="time")
    @JsonProperty(value="horario")
    private @NotNull(message="{agendamento.horario.notNull}") LocalTime horario;
    @Min(value=1L, message="{agendamento.frequencia.min}")
    @Max(value=4L, message="{agendamento.frequencia.max}")
    @Schema(description="N\u00famero de vezes por dia que o medicamento ser\u00e1 tomado (1 a 4)", example="2", required=true)
    @JsonProperty(value="frequencia")
    private @Min(value=1L, message="{agendamento.frequencia.min}") @Max(value=4L, message="{agendamento.frequencia.max}") int frequencia;
    @Size(max=500, message="{agendamento.observacoes.size}")
    @Schema(description="Observa\u00e7\u00f5es do paciente ou do farmac\u00eautico", example="Tomar com \u00e1gua. Jejum obrigat\u00f3rio.")
    @JsonProperty(value="observacoes")
    private @Size(max=500, message="{agendamento.observacoes.size}") String observacoes;
    @Schema(description="Se deve enviar lembretes de notifica\u00e7\u00e3o para o cliente", example="true", required=true)
    @JsonProperty(value="enviarNotificacoes")
    private boolean enviarNotificacoes;
    @NotBlank(message="{agendamento.tenantId.notBlank}")
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001", required=true)
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{agendamento.tenantId.notBlank}") String tenantId;
    @Schema(description="Token de rastreamento (UUID) para auditoria", example="4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @PastOrPresent(message="{agendamento.criadoEm.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description="Data/hora de cria\u00e7\u00e3o do registro", example="2025-07-07T09:30:00", type="string", format="date-time")
    @JsonProperty(value="criadoEm")
    private @PastOrPresent(message="{agendamento.criadoEm.pastOrPresent}") LocalDateTime criadoEm;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description="Data/hora da \u00faltima atualiza\u00e7\u00e3o do registro", example="2025-07-08T10:00:00", type="string", format="date-time")
    @JsonProperty(value="atualizadoEm")
    private LocalDateTime atualizadoEm;

    public AgendamentoMedicacaoRequestDTO() {
    }

    public AgendamentoMedicacaoRequestDTO(UUID agendamentoId, Long clienteId, String nomeMedicamento, LocalDate dataInicio, LocalTime horario, int frequencia, String observacoes, boolean enviarNotificacoes, String tenantId, UUID traceId, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
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

    public UUID getAgendamentoId() {
        return this.agendamentoId;
    }

    public void setAgendamentoId(UUID agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeMedicamento() {
        return this.nomeMedicamento;
    }

    public void setNomeMedicamento(String nomeMedicamento) {
        this.nomeMedicamento = nomeMedicamento;
    }

    public LocalDate getDataInicio() {
        return this.dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalTime getHorario() {
        return this.horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public int getFrequencia() {
        return this.frequencia;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
    }

    public String getObservacoes() {
        return this.observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isEnviarNotificacoes() {
        return this.enviarNotificacoes;
    }

    public void setEnviarNotificacoes(boolean enviarNotificacoes) {
        this.enviarNotificacoes = enviarNotificacoes;
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

    public LocalDateTime getCriadoEm() {
        return this.criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return this.atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgendamentoMedicacaoRequestDTO)) {
            return false;
        }
        AgendamentoMedicacaoRequestDTO that = (AgendamentoMedicacaoRequestDTO)o;
        return this.frequencia == that.frequencia && this.enviarNotificacoes == that.enviarNotificacoes && Objects.equals(this.agendamentoId, that.agendamentoId) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.nomeMedicamento, that.nomeMedicamento) && Objects.equals(this.dataInicio, that.dataInicio) && Objects.equals(this.horario, that.horario) && Objects.equals(this.observacoes, that.observacoes) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.criadoEm, that.criadoEm) && Objects.equals(this.atualizadoEm, that.atualizadoEm);
    }

    public int hashCode() {
        return Objects.hash(this.agendamentoId, this.clienteId, this.nomeMedicamento, this.dataInicio, this.horario, this.frequencia, this.observacoes, this.enviarNotificacoes, this.tenantId, this.traceId, this.criadoEm, this.atualizadoEm);
    }

    public String toString() {
        return "AgendamentoMedicacaoRequestDTO{agendamentoId=" + String.valueOf(this.agendamentoId) + ", clienteId=" + this.clienteId + ", nomeMedicamento='" + this.nomeMedicamento + "', dataInicio=" + String.valueOf(this.dataInicio) + ", horario=" + String.valueOf(this.horario) + ", frequencia=" + this.frequencia + ", observacoes='" + this.observacoes + "', enviarNotificacoes=" + this.enviarNotificacoes + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", criadoEm=" + String.valueOf(this.criadoEm) + ", atualizadoEm=" + String.valueOf(this.atualizadoEm) + "}";
    }
}

