/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$AccessMode
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.DecimalMax
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Max
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="MensagemIARequestDTO", description="Dados para requisi\u00e7\u00e3o de mensagem \u00e0 IA RedeMaisFarma")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class MensagemIARequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico da requisi\u00e7\u00e3o (UUID) gerado pelo servidor", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode=Schema.AccessMode.READ_ONLY)
    @JsonProperty(value="requestId", access=JsonProperty.Access.READ_ONLY)
    private UUID requestId;
    @Schema(description="ID do cliente", example="123", required=true)
    @NotNull(message="{mensagemIA.clienteId.notNull}")
    @Min(value=1L, message="{mensagemIA.clienteId.min}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{mensagemIA.clienteId.notNull}") @Min(value=1L, message="{mensagemIA.clienteId.min}") Long clienteId;
    @Schema(description="Conte\u00fado da mensagem para IA", example="Qual a dosagem recomendada para dor de cabe\u00e7a?", required=true)
    @NotBlank(message="{mensagemIA.mensagem.notBlank}")
    @Size(min=1, max=2000, message="{mensagemIA.mensagem.size}")
    @JsonProperty(value="mensagem")
    private @NotBlank(message="{mensagemIA.mensagem.notBlank}") @Size(min=1, max=2000, message="{mensagemIA.mensagem.size}") String mensagem;
    @Schema(description="Hist\u00f3rico de mensagens (contexto) para a IA")
    @Size(max=20, message="{mensagemIA.contexto.size}")
    @Valid
    @JsonProperty(value="contexto")
    private @Size(max=20, message="{mensagemIA.contexto.size}") @Valid List<@NotBlank @Size(max=2000) String> contexto;
    @Schema(description="Modelo de IA a ser usado", example="gpt-4", allowableValues={"gpt-4", "gpt-4o", "gpt-4-mini", "gpt-3.5-turbo"}, required=true)
    @NotBlank(message="{mensagemIA.modelo.notBlank}")
    @Size(max=50, message="{mensagemIA.modelo.size}")
    @JsonProperty(value="modelo")
    private @NotBlank(message="{mensagemIA.modelo.notBlank}") @Size(max=50, message="{mensagemIA.modelo.size}") String modelo = "gpt-4";
    @Schema(description="Temperatura de gera\u00e7\u00e3o (0.0-1.0)", example="0.7")
    @DecimalMin(value="0.0", inclusive=true, message="{mensagemIA.temperatura.min}")
    @DecimalMax(value="1.0", inclusive=true, message="{mensagemIA.temperatura.max}")
    @JsonProperty(value="temperatura")
    private @DecimalMin(value="0.0", inclusive=true, message="{mensagemIA.temperatura.min}") @DecimalMax(value="1.0", inclusive=true, message="{mensagemIA.temperatura.max}") Double temperatura = 0.7;
    @Schema(description="M\u00e1ximo de tokens de resposta", example="512")
    @Min(value=1L, message="{mensagemIA.maxTokens.min}")
    @Max(value=2048L, message="{mensagemIA.maxTokens.max}")
    @JsonProperty(value="maxTokens")
    private @Min(value=1L, message="{mensagemIA.maxTokens.min}") @Max(value=2048L, message="{mensagemIA.maxTokens.max}") Integer maxTokens = 512;
    @Schema(description="Salvar intera\u00e7\u00e3o em log para auditoria", example="true")
    @JsonProperty(value="salvarLog")
    private Boolean salvarLog = Boolean.TRUE;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001", required=true)
    @NotBlank(message="{mensagemIA.tenantId.notBlank}")
    @Size(max=100, message="{mensagemIA.tenantId.size}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{mensagemIA.tenantId.notBlank}") @Size(max=100, message="{mensagemIA.tenantId.size}") String tenantId;
    @Schema(description="Token de rastreamento (UUID) para troubleshooting", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Data/hora da requisi\u00e7\u00e3o (gerado pelo servidor)", type="string", format="date-time", example="2025-07-04T15:00:00", accessMode=Schema.AccessMode.READ_ONLY)
    @PastOrPresent(message="{mensagemIA.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataEnvio", access=JsonProperty.Access.READ_ONLY)
    private @PastOrPresent(message="{mensagemIA.dataEnvio.pastOrPresent}") LocalDateTime dataEnvio;

    public MensagemIARequestDTO() {
    }

    public MensagemIARequestDTO(UUID requestId, Long clienteId, String mensagem, List<String> contexto, String modelo, Double temperatura, Integer maxTokens, Boolean salvarLog, String tenantId, UUID traceId, LocalDateTime dataEnvio) {
        this.requestId = requestId;
        this.clienteId = clienteId;
        this.mensagem = mensagem;
        this.contexto = contexto;
        this.modelo = modelo;
        this.temperatura = temperatura;
        this.maxTokens = maxTokens;
        this.salvarLog = salvarLog;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.dataEnvio = dataEnvio;
    }

    public UUID getRequestId() {
        return this.requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<String> getContexto() {
        return this.contexto;
    }

    public void setContexto(List<String> contexto) {
        this.contexto = contexto;
    }

    public String getModelo() {
        return this.modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Double getTemperatura() {
        return this.temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getMaxTokens() {
        return this.maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getSalvarLog() {
        return this.salvarLog;
    }

    public void setSalvarLog(Boolean salvarLog) {
        this.salvarLog = salvarLog;
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

    public LocalDateTime getDataEnvio() {
        return this.dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MensagemIARequestDTO)) {
            return false;
        }
        MensagemIARequestDTO that = (MensagemIARequestDTO)o;
        return Objects.equals(this.requestId, that.requestId) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.mensagem, that.mensagem) && Objects.equals(this.contexto, that.contexto) && Objects.equals(this.modelo, that.modelo) && Objects.equals(this.temperatura, that.temperatura) && Objects.equals(this.maxTokens, that.maxTokens) && Objects.equals(this.salvarLog, that.salvarLog) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.dataEnvio, that.dataEnvio);
    }

    public int hashCode() {
        return Objects.hash(this.requestId, this.clienteId, this.mensagem, this.contexto, this.modelo, this.temperatura, this.maxTokens, this.salvarLog, this.tenantId, this.traceId, this.dataEnvio);
    }

    public String toString() {
        return "MensagemIARequestDTO{requestId=" + String.valueOf(this.requestId) + ", clienteId=" + this.clienteId + ", mensagem='" + this.mensagem + "', contexto=" + String.valueOf(this.contexto) + ", modelo='" + this.modelo + "', temperatura=" + this.temperatura + ", maxTokens=" + this.maxTokens + ", salvarLog=" + this.salvarLog + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", dataEnvio=" + String.valueOf(this.dataEnvio) + "}";
    }
}

