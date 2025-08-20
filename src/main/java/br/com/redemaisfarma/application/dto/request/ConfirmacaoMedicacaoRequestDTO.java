package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ConfirmacaoMedicacaoRequestDTO", description = "Dados de confirmação de medicação do cliente")
public class ConfirmacaoMedicacaoRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do cliente", example = "123", required = true)
    @NotNull(message = "{confirmacaoMedicacao.clienteId.notNull}")
    @Min(value = 1, message = "{confirmacaoMedicacao.clienteId.min}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "ID da confirmação", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value = "confirmacaoId", access = JsonProperty.Access.READ_ONLY)
    private UUID confirmacaoId;

    @Schema(description = "ID do lembrete associado", example = "4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("lembreteId")
    private UUID lembreteId;

    @Schema(description = "Nome do medicamento", example = "Dipirona 500mg", required = true)
    @NotBlank(message = "{confirmacaoMedicacao.nomeMedicamento.notBlank}")
    @Size(max = 100, message = "{confirmacaoMedicacao.nomeMedicamento.size}")
    @JsonProperty("nomeMedicamento")
    private String nomeMedicamento;

    @Schema(description = "Horário agendado para ingestão do medicamento", type = "string", format = "date-time", example = "2025-07-04T08:00:00", required = true)
    @NotNull(message = "{confirmacaoMedicacao.horarioAgendado.notNull}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("horarioAgendado")
    private LocalDateTime horarioAgendado;

    @Schema(description = "Horário em que a medicação foi confirmada", type = "string", format = "date-time", example = "2025-07-04T08:05:00", required = true)
    @NotNull(message = "{confirmacaoMedicacao.horarioConfirmado.notNull}")
    @PastOrPresent(message = "{confirmacaoMedicacao.horarioConfirmado.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("horarioConfirmado")
    private LocalDateTime horarioConfirmado;

    @Schema(description = "Indicador se o medicamento foi tomado", required = true)
    @NotNull(message = "{confirmacaoMedicacao.tomou.notNull}")
    @JsonProperty("tomou")
    private Boolean tomou;

    @Schema(description = "Indica se a confirmação foi automática pelo sistema", example = "false")
    @NotNull(message = "{confirmacaoMedicacao.automatico.notNull}")
    @JsonProperty("automatico")
    private Boolean automatico = Boolean.FALSE;

    @Schema(description = "Observações adicionais", example = "Cliente relatou leve tontura")
    @Size(max = 500, message = "{confirmacaoMedicacao.observacao.size}")
    @JsonProperty("observacao")
    private String observacao;

    @Schema(description = "ID do tenant", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{confirmacaoMedicacao.tenantId.notBlank}")
    @Size(max = 100, message = "{confirmacaoMedicacao.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value = "traceId")
    private UUID traceId;

    @Schema(description = "Data e hora de criação do registro", type = "string", format = "date-time", example = "2025-07-04T08:05:00", required = true)
    @NotNull(message = "{confirmacaoMedicacao.dataCriacao.notNull}")
    @PastOrPresent(message = "{confirmacaoMedicacao.dataCriacao.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "dataCriacao", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataCriacao;

    @Schema(description = "Data e hora da última atualização do registro", type = "string", format = "date-time", example = "2025-07-04T08:10:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "dataAtualizacao", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataAtualizacao;

    // ---- Consistência de dados
    @AssertTrue(message = "{confirmacaoMedicacao.horarios.ordemValida}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isOrdemHorariosValida() {
        if (horarioAgendado == null || horarioConfirmado == null)
            return false;
        return !horarioConfirmado.isBefore(horarioAgendado);
    }

    @AssertTrue(message = "{confirmacaoMedicacao.automatico.requerLembrete}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isAutomaticoComLembrete() {
        if (automatico == null)
            return false;
        if (automatico)
            return lembreteId != null;
        return true;
    }

    @AssertTrue(message = "{confirmacaoMedicacao.tomou.requerConfirmado}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isTomouRegras() {
        if (tomou == null)
            return false;
        if (tomou)
            return horarioConfirmado != null;
        return true;
    }

    // ---- Construtores
    public ConfirmacaoMedicacaoRequestDTO() {
    }

    public ConfirmacaoMedicacaoRequestDTO(Long clienteId, UUID confirmacaoId, UUID lembreteId, String nomeMedicamento,
            LocalDateTime horarioAgendado, LocalDateTime horarioConfirmado, Boolean tomou, Boolean automatico,
            String observacao, String tenantId, UUID traceId, LocalDateTime dataCriacao,
            LocalDateTime dataAtualizacao) {
        this.clienteId = clienteId;
        this.confirmacaoId = confirmacaoId;
        this.lembreteId = lembreteId;
        this.nomeMedicamento = nomeMedicamento;
        this.horarioAgendado = horarioAgendado;
        this.horarioConfirmado = horarioConfirmado;
        this.tomou = tomou;
        this.automatico = automatico;
        this.observacao = observacao;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.dataCriacao = dataCriacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    // ---- Getters/Setters (inalterados)

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public UUID getConfirmacaoId() {
        return confirmacaoId;
    }

    public void setConfirmacaoId(UUID confirmacaoId) {
        this.confirmacaoId = confirmacaoId;
    }

    public UUID getLembreteId() {
        return lembreteId;
    }

    public void setLembreteId(UUID lembreteId) {
        this.lembreteId = lembreteId;
    }

    public String getNomeMedicamento() {
        return nomeMedicamento;
    }

    public void setNomeMedicamento(String nomeMedicamento) {
        this.nomeMedicamento = nomeMedicamento;
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

    public Boolean getTomou() {
        return tomou;
    }

    public void setTomou(Boolean tomou) {
        this.tomou = tomou;
    }

    public Boolean getAutomatico() {
        return automatico;
    }

    public void setAutomatico(Boolean automatico) {
        this.automatico = automatico;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    // ---- util
    @Override
    public boolean equals(Object o) { /* igual ao seu */
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfirmacaoMedicacaoRequestDTO that = (ConfirmacaoMedicacaoRequestDTO) o;
        return Objects.equals(clienteId, that.clienteId) && Objects.equals(confirmacaoId, that.confirmacaoId)
                && Objects.equals(lembreteId, that.lembreteId) && Objects.equals(nomeMedicamento, that.nomeMedicamento)
                && Objects.equals(horarioAgendado, that.horarioAgendado)
                && Objects.equals(horarioConfirmado, that.horarioConfirmado) && Objects.equals(tomou, that.tomou)
                && Objects.equals(automatico, that.automatico) && Objects.equals(observacao, that.observacao)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(dataCriacao, that.dataCriacao)
                && Objects.equals(dataAtualizacao, that.dataAtualizacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, confirmacaoId, lembreteId, nomeMedicamento, horarioAgendado, horarioConfirmado,
                tomou, automatico, observacao, tenantId, traceId, dataCriacao, dataAtualizacao);
    }

    @Override
    public String toString() { /* igual ao seu */
        return "ConfirmacaoMedicacaoRequestDTO{" + "clienteId=" + clienteId + ", confirmacaoId=" + confirmacaoId
                + ", lembreteId=" + lembreteId + ", nomeMedicamento='" + nomeMedicamento + '\'' + ", horarioAgendado="
                + horarioAgendado + ", horarioConfirmado=" + horarioConfirmado + ", tomou=" + tomou + ", automatico="
                + automatico + ", observacao='" + observacao + '\'' + ", tenantId='" + tenantId + '\'' + ", traceId="
                + traceId + ", dataCriacao=" + dataCriacao + ", dataAtualizacao=" + dataAtualizacao + '}';
    }
}
