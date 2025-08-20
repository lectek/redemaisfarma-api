package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de resposta para retornos de agendamentos de medicação na API RedeMaisFarma. Somente dados de saída (sem Bean
 * Validation). Campos nulos são omitidos no JSON.
 */
@Schema(name = "AgendamentoMedicacaoResponseDTO", description = "Dados de agendamento de medicação do cliente")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgendamentoMedicacaoResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único do agendamento", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("agendamentoId")
    private UUID agendamentoId;

    @Schema(description = "ID do cliente", example = "12345")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Data de início do agendamento", type = "string", format = "date", example = "2025-07-04")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("dataInicio")
    private LocalDate dataInicio;

    @Schema(description = "Data de término do agendamento", type = "string", format = "date", example = "2025-07-10")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("dataFim")
    private LocalDate dataFim;

    @Schema(description = "Lista de horários de administração no formato HH:mm:ss", example = "[\"08:00:00\", \"20:00:00\"]")
    @JsonProperty("horarios")
    private List<String> horarios;

    @Schema(description = "Dosagem prevista para cada administração", example = "500mg")
    @JsonProperty("dosagem")
    private String dosagem;

    @Schema(description = "Frequência em dias entre administrações", example = "1")
    @JsonProperty("frequenciaDias")
    private Integer frequenciaDias;

    @Schema(description = "Status do agendamento", example = "ATIVO")
    @JsonProperty("status")
    private StatusAgendamento status;

    @Schema(description = "Lista de registros de confirmação de dose e ações")
    @JsonProperty("historico")
    private List<HistoricoDoseDTO> historico;

    @Schema(description = "Data/hora de criação do agendamento", type = "string", format = "date-time", example = "2025-07-04T09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("criadoEm")
    private LocalDateTime criadoEm;

    @Schema(description = "Data/hora da última atualização", type = "string", format = "date-time", example = "2025-07-05T10:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("atualizadoEm")
    private LocalDateTime atualizadoEm;

    // Construtores
    public AgendamentoMedicacaoResponseDTO() {
    }

    public AgendamentoMedicacaoResponseDTO(UUID agendamentoId, Long clienteId, LocalDate dataInicio, LocalDate dataFim,
            List<String> horarios, String dosagem, Integer frequenciaDias, StatusAgendamento status,
            List<HistoricoDoseDTO> historico, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.agendamentoId = agendamentoId;
        this.clienteId = clienteId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.horarios = horarios;
        this.dosagem = dosagem;
        this.frequenciaDias = frequenciaDias;
        this.status = status;
        this.historico = historico;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    // Getters e Setters
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

    public List<String> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<String> horarios) {
        this.horarios = horarios;
    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

    public Integer getFrequenciaDias() {
        return frequenciaDias;
    }

    public void setFrequenciaDias(Integer frequenciaDias) {
        this.frequenciaDias = frequenciaDias;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public List<HistoricoDoseDTO> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoDoseDTO> historico) {
        this.historico = historico;
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
        if (!(o instanceof AgendamentoMedicacaoResponseDTO))
            return false;
        AgendamentoMedicacaoResponseDTO that = (AgendamentoMedicacaoResponseDTO) o;
        return Objects.equals(agendamentoId, that.agendamentoId) && Objects.equals(clienteId, that.clienteId)
                && Objects.equals(dataInicio, that.dataInicio) && Objects.equals(dataFim, that.dataFim)
                && Objects.equals(horarios, that.horarios) && Objects.equals(dosagem, that.dosagem)
                && Objects.equals(frequenciaDias, that.frequenciaDias) && status == that.status
                && Objects.equals(historico, that.historico) && Objects.equals(criadoEm, that.criadoEm)
                && Objects.equals(atualizadoEm, that.atualizadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agendamentoId, clienteId, dataInicio, dataFim, horarios, dosagem, frequenciaDias, status,
                historico, criadoEm, atualizadoEm);
    }

    @Override
    public String toString() {
        return "AgendamentoMedicacaoResponseDTO{" + "agendamentoId=" + agendamentoId + ", clienteId=" + clienteId
                + ", dataInicio=" + dataInicio + ", dataFim=" + dataFim + ", horarios=" + horarios + ", dosagem='"
                + dosagem + '\'' + ", frequenciaDias=" + frequenciaDias + ", status=" + status + ", historico="
                + historico + ", criadoEm=" + criadoEm + ", atualizadoEm=" + atualizadoEm + '}';
    }

    // Enum
    public enum StatusAgendamento {
        ATIVO, PAUSADO, CONCLUIDO, CANCELADO
    }

    // DTO interno
    @Schema(name = "HistoricoDoseDTO", description = "Registro de confirmação de dose administrada")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HistoricoDoseDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID único do registro de dose", example = "4fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("registroId")
        private UUID registroId;

        @Schema(description = "Data e hora da administração", type = "string", format = "date-time", example = "2025-07-04T08:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("registroEm")
        private LocalDateTime registroEm;

        @Schema(description = "Indicador se a dose foi confirmada pelo usuário", example = "true")
        @JsonProperty("confirmado")
        private Boolean confirmado;

        @Schema(description = "Observações do registro", example = "Paciente relatou leve tontura após dose")
        @JsonProperty("observacoes")
        private String observacoes;

        public HistoricoDoseDTO() {
        }

        public HistoricoDoseDTO(UUID registroId, LocalDateTime registroEm, Boolean confirmado, String observacoes) {
            this.registroId = registroId;
            this.registroEm = registroEm;
            this.confirmado = confirmado;
            this.observacoes = observacoes;
        }

        public UUID getRegistroId() {
            return registroId;
        }

        public void setRegistroId(UUID registroId) {
            this.registroId = registroId;
        }

        public LocalDateTime getRegistroEm() {
            return registroEm;
        }

        public void setRegistroEm(LocalDateTime registroEm) {
            this.registroEm = registroEm;
        }

        public Boolean getConfirmado() {
            return confirmado;
        }

        public void setConfirmado(Boolean confirmado) {
            this.confirmado = confirmado;
        }

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof HistoricoDoseDTO))
                return false;
            HistoricoDoseDTO that = (HistoricoDoseDTO) o;
            return Objects.equals(registroId, that.registroId) && Objects.equals(registroEm, that.registroEm)
                    && Objects.equals(confirmado, that.confirmado) && Objects.equals(observacoes, that.observacoes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(registroId, registroEm, confirmado, observacoes);
        }

        @Override
        public String toString() {
            return "HistoricoDoseDTO{" + "registroId=" + registroId + ", registroEm=" + registroEm + ", confirmado="
                    + confirmado + ", observacoes='" + observacoes + '\'' + '}';
        }
    }
}
