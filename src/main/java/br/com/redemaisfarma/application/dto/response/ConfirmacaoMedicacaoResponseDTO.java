package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para confirmação de medicação de um cliente na API RedeMaisFarma.
 */
@Schema(name = "ConfirmacaoMedicacaoResponseDTO", description = "Detalhes da confirmação de medicação")
public class ConfirmacaoMedicacaoResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único da confirmação", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("confirmacaoId")
    private UUID confirmacaoId;

    @Schema(description = "ID do cliente", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{confirmacaoMedicacao.clienteId.notNull}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Nome do medicamento", example = "Dipirona 500mg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{confirmacaoMedicacao.medicamento.notBlank}")
    @Size(max = 100, message = "{confirmacaoMedicacao.medicamento.size}")
    @JsonProperty("medicamento")
    private String medicamento;

    @Schema(description = "Data de início do tratamento", type = "string", format = "date", example = "2025-07-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("dataInicioTratamento")
    private LocalDate dataInicioTratamento;

    @Schema(description = "Horário agendado", type = "string", format = "date-time", example = "2025-07-04T08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{confirmacaoMedicacao.horarioAgendado.notNull}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("horarioAgendado")
    private LocalDateTime horarioAgendado;

    @Schema(description = "Horário de confirmação", type = "string", format = "date-time", example = "2025-07-04T08:05:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{confirmacaoMedicacao.horarioConfirmado.notNull}")
    @PastOrPresent(message = "{confirmacaoMedicacao.horarioConfirmado.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("horarioConfirmado")
    private LocalDateTime horarioConfirmado;

    @Schema(description = "Indicador de tomada do medicamento", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{confirmacaoMedicacao.confirmado.notNull}")
    @JsonProperty("confirmado")
    private Boolean confirmado;

    @Schema(description = "Método de confirmação", example = "MANUAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{confirmacaoMedicacao.metodoConfirmacao.notNull}")
    @JsonProperty("metodoConfirmacao")
    private MetodoConfirmacao metodoConfirmacao;

    @Schema(description = "ID do lembrete associado (UUID)", example = "4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("lembreteId")
    private UUID lembreteId;

    @Schema(description = "Desconto automático aplicado (%)", example = "5.0")
    @DecimalMin(value = "0.0", inclusive = true, message = "{confirmacaoMedicacao.descontoAplicado.min}")
    @DecimalMax(value = "100.0", inclusive = true, message = "{confirmacaoMedicacao.descontoAplicado.max}")
    @JsonProperty("descontoAplicado")
    private BigDecimal descontoAplicado = BigDecimal.ZERO;

    @Schema(description = "Observações adicionais", example = "Paciente relatou leve tontura.")
    @Size(max = 500, message = "{confirmacaoMedicacao.observacoes.size}")
    @JsonProperty("observacoes")
    private String observacoes;

    @Schema(description = "Status de adesão ao tratamento", example = "EM_ANDAMENTO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{confirmacaoMedicacao.statusAdesao.notNull}")
    @JsonProperty("statusAdesao")
    private StatusAdesao statusAdesao;

    @Schema(description = "Timestamp de criação do registro", type = "string", format = "date-time", example = "2025-07-04T08:05:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{confirmacaoMedicacao.criadoEm.notNull}")
    @PastOrPresent(message = "{confirmacaoMedicacao.criadoEm.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("criadoEm")
    private LocalDateTime criadoEm;

    @Schema(description = "Timestamp da última atualização", type = "string", format = "date-time", example = "2025-07-04T08:10:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("atualizadoEm")
    private LocalDateTime atualizadoEm;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{confirmacaoMedicacao.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)", example = "5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Histórico de confirmações")
    @JsonProperty("historico")
    private List<@Valid HistoricoConfirmacaoDTO> historico;

    // Construtores

    public ConfirmacaoMedicacaoResponseDTO() {
    }

    public ConfirmacaoMedicacaoResponseDTO(UUID confirmacaoId, Long clienteId, String medicamento,
            LocalDate dataInicioTratamento, LocalDateTime horarioAgendado, LocalDateTime horarioConfirmado,
            Boolean confirmado, MetodoConfirmacao metodoConfirmacao, UUID lembreteId, BigDecimal descontoAplicado,
            String observacoes, StatusAdesao statusAdesao, LocalDateTime criadoEm, LocalDateTime atualizadoEm,
            String tenantId, UUID traceId, List<HistoricoConfirmacaoDTO> historico) {

        this.confirmacaoId = confirmacaoId;
        this.clienteId = clienteId;
        this.medicamento = medicamento;
        this.dataInicioTratamento = dataInicioTratamento;
        this.horarioAgendado = horarioAgendado;
        this.horarioConfirmado = horarioConfirmado;
        this.confirmado = confirmado;
        this.metodoConfirmacao = metodoConfirmacao;
        this.lembreteId = lembreteId;
        this.descontoAplicado = descontoAplicado;
        this.observacoes = observacoes;
        this.statusAdesao = statusAdesao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.historico = historico;
    }

    // Getters/Setters

    public UUID getConfirmacaoId() {
        return confirmacaoId;
    }

    public void setConfirmacaoId(UUID confirmacaoId) {
        this.confirmacaoId = confirmacaoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public LocalDate getDataInicioTratamento() {
        return dataInicioTratamento;
    }

    public void setDataInicioTratamento(LocalDate dataInicioTratamento) {
        this.dataInicioTratamento = dataInicioTratamento;
    }

    public LocalDateTime getHorarioAgendado() {
        return horarioAgendado;
    }

    public void setHorarioAgendado(LocalDateTime horarioAgendado) {
        this.horarioAgendado = horarioAgendado;
    }

    public LocalDateTime getHorarioConfirmado() {
        return horarioConfirmado;
    }

    public void setHorarioConfirmado(LocalDateTime horarioConfirmado) {
        this.horarioConfirmado = horarioConfirmado;
    }

    public Boolean getConfirmado() {
        return confirmado;
    }

    public void setConfirmado(Boolean confirmado) {
        this.confirmado = confirmado;
    }

    public MetodoConfirmacao getMetodoConfirmacao() {
        return metodoConfirmacao;
    }

    public void setMetodoConfirmacao(MetodoConfirmacao metodoConfirmacao) {
        this.metodoConfirmacao = metodoConfirmacao;
    }

    public UUID getLembreteId() {
        return lembreteId;
    }

    public void setLembreteId(UUID lembreteId) {
        this.lembreteId = lembreteId;
    }

    public BigDecimal getDescontoAplicado() {
        return descontoAplicado;
    }

    public void setDescontoAplicado(BigDecimal descontoAplicado) {
        this.descontoAplicado = descontoAplicado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public StatusAdesao getStatusAdesao() {
        return statusAdesao;
    }

    public void setStatusAdesao(StatusAdesao statusAdesao) {
        this.statusAdesao = statusAdesao;
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

    public List<HistoricoConfirmacaoDTO> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoConfirmacaoDTO> historico) {
        this.historico = historico;
    }

    // utilitários

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ConfirmacaoMedicacaoResponseDTO))
            return false;
        ConfirmacaoMedicacaoResponseDTO that = (ConfirmacaoMedicacaoResponseDTO) o;
        return Objects.equals(confirmacaoId, that.confirmacaoId) && Objects.equals(clienteId, that.clienteId)
                && Objects.equals(medicamento, that.medicamento)
                && Objects.equals(dataInicioTratamento, that.dataInicioTratamento)
                && Objects.equals(horarioAgendado, that.horarioAgendado)
                && Objects.equals(horarioConfirmado, that.horarioConfirmado)
                && Objects.equals(confirmado, that.confirmado) && metodoConfirmacao == that.metodoConfirmacao
                && Objects.equals(lembreteId, that.lembreteId)
                && Objects.equals(descontoAplicado, that.descontoAplicado)
                && Objects.equals(observacoes, that.observacoes) && statusAdesao == that.statusAdesao
                && Objects.equals(criadoEm, that.criadoEm) && Objects.equals(atualizadoEm, that.atualizadoEm)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(historico, that.historico);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirmacaoId, clienteId, medicamento, dataInicioTratamento, horarioAgendado,
                horarioConfirmado, confirmado, metodoConfirmacao, lembreteId, descontoAplicado, observacoes,
                statusAdesao, criadoEm, atualizadoEm, tenantId, traceId, historico);
    }

    @Override
    public String toString() {
        return "ConfirmacaoMedicacaoResponseDTO{" + "confirmacaoId=" + confirmacaoId + ", clienteId=" + clienteId
                + ", medicamento='" + medicamento + '\'' + ", dataInicioTratamento=" + dataInicioTratamento
                + ", horarioAgendado=" + horarioAgendado + ", horarioConfirmado=" + horarioConfirmado + ", confirmado="
                + confirmado + ", metodoConfirmacao=" + metodoConfirmacao + ", lembreteId=" + lembreteId
                + ", descontoAplicado=" + descontoAplicado + ", observacoes='" + observacoes + '\'' + ", statusAdesao="
                + statusAdesao + ", criadoEm=" + criadoEm + ", atualizadoEm=" + atualizadoEm + ", tenantId='" + tenantId
                + '\'' + ", traceId=" + traceId + ", historico=" + historico + '}';
    }

    // Enums
    public enum MetodoConfirmacao {
        MANUAL, AUTOMATICO
    }

    public enum StatusAdesao {
        EM_ANDAMENTO, CONCLUIDA, ATRASADA, CANCELADA
    }

    // DTO interno: Histórico
    @Schema(name = "HistoricoConfirmacaoDTO", description = "Registro de cada confirmação de dose")
    public static class HistoricoConfirmacaoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID único do registro", example = "6fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("registroId")
        private UUID registroId;

        @Schema(description = "Horário do registro", type = "string", format = "date-time", example = "2025-07-04T08:05:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{historicoConfirmacao.registroEm.notNull}")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("registroEm")
        private LocalDateTime registroEm;

        @Schema(description = "Detalhes adicionais do registro", example = "Confirmação via app móvel")
        @Size(max = 200, message = "{historicoConfirmacao.detalhes.size}")
        @JsonProperty("detalhes")
        private String detalhes;

        public HistoricoConfirmacaoDTO() {
        }

        public HistoricoConfirmacaoDTO(UUID registroId, LocalDateTime registroEm, String detalhes) {
            this.registroId = registroId;
            this.registroEm = registroEm;
            this.detalhes = detalhes;
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

        public String getDetalhes() {
            return detalhes;
        }

        public void setDetalhes(String detalhes) {
            this.detalhes = detalhes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof HistoricoConfirmacaoDTO))
                return false;
            HistoricoConfirmacaoDTO that = (HistoricoConfirmacaoDTO) o;
            return Objects.equals(registroId, that.registroId) && Objects.equals(registroEm, that.registroEm)
                    && Objects.equals(detalhes, that.detalhes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(registroId, registroEm, detalhes);
        }

        @Override
        public String toString() {
            return "HistoricoConfirmacaoDTO{" + "registroId=" + registroId + ", registroEm=" + registroEm
                    + ", detalhes='" + detalhes + '\'' + '}';
        }
    }
}
