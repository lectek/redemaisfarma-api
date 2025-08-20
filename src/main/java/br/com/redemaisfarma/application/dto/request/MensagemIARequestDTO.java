package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de requisição para envio de mensagens à IA da RedeMaisFarma. - Campos de auditoria gerenciados pelo servidor
 * (somente leitura) - Validações de tamanho e limites de parâmetros - Compatível com MapStruct e OpenAPI
 */
@Schema(name = "MensagemIARequestDTO", description = "Dados para requisição de mensagem à IA RedeMaisFarma")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MensagemIARequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único da requisição (UUID) gerado pelo servidor", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(value = "requestId", access = JsonProperty.Access.READ_ONLY)
    private UUID requestId;

    @Schema(description = "ID do cliente", example = "123", required = true)
    @NotNull(message = "{mensagemIA.clienteId.notNull}")
    @Min(value = 1, message = "{mensagemIA.clienteId.min}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Conteúdo da mensagem para IA", example = "Qual a dosagem recomendada para dor de cabeça?", required = true)
    @NotBlank(message = "{mensagemIA.mensagem.notBlank}")
    @Size(min = 1, max = 2000, message = "{mensagemIA.mensagem.size}")
    @JsonProperty("mensagem")
    private String mensagem;

    @Schema(description = "Histórico de mensagens (contexto) para a IA")
    @Size(max = 20, message = "{mensagemIA.contexto.size}")
    @Valid
    @JsonProperty("contexto")
    private List<@NotBlank @Size(max = 2000) String> contexto;

    @Schema(description = "Modelo de IA a ser usado", example = "gpt-4", allowableValues = { "gpt-4", "gpt-4o",
            "gpt-4-mini", "gpt-3.5-turbo" }, required = true)
    @NotBlank(message = "{mensagemIA.modelo.notBlank}")
    @Size(max = 50, message = "{mensagemIA.modelo.size}")
    @JsonProperty("modelo")
    private String modelo = "gpt-4";

    @Schema(description = "Temperatura de geração (0.0-1.0)", example = "0.7")
    @DecimalMin(value = "0.0", inclusive = true, message = "{mensagemIA.temperatura.min}")
    @DecimalMax(value = "1.0", inclusive = true, message = "{mensagemIA.temperatura.max}")
    @JsonProperty("temperatura")
    private Double temperatura = 0.7;

    @Schema(description = "Máximo de tokens de resposta", example = "512")
    @Min(value = 1, message = "{mensagemIA.maxTokens.min}")
    @Max(value = 2048, message = "{mensagemIA.maxTokens.max}")
    @JsonProperty("maxTokens")
    private Integer maxTokens = 512;

    @Schema(description = "Salvar interação em log para auditoria", example = "true")
    @JsonProperty("salvarLog")
    private Boolean salvarLog = Boolean.TRUE;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{mensagemIA.tenantId.notBlank}")
    @Size(max = 100, message = "{mensagemIA.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID) para troubleshooting", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Data/hora da requisição (gerado pelo servidor)", type = "string", format = "date-time", example = "2025-07-04T15:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    @PastOrPresent(message = "{mensagemIA.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "dataEnvio", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataEnvio;

    // Construtores
    public MensagemIARequestDTO() {
    }

    public MensagemIARequestDTO(UUID requestId, Long clienteId, String mensagem, List<String> contexto, String modelo,
            Double temperatura, Integer maxTokens, Boolean salvarLog, String tenantId, UUID traceId,
            LocalDateTime dataEnvio) {
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

    // Getters/Setters
    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<String> getContexto() {
        return contexto;
    }

    public void setContexto(List<String> contexto) {
        this.contexto = contexto;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getSalvarLog() {
        return salvarLog;
    }

    public void setSalvarLog(Boolean salvarLog) {
        this.salvarLog = salvarLog;
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

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    // Utilitários
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MensagemIARequestDTO that))
            return false;
        return Objects.equals(requestId, that.requestId) && Objects.equals(clienteId, that.clienteId)
                && Objects.equals(mensagem, that.mensagem) && Objects.equals(contexto, that.contexto)
                && Objects.equals(modelo, that.modelo) && Objects.equals(temperatura, that.temperatura)
                && Objects.equals(maxTokens, that.maxTokens) && Objects.equals(salvarLog, that.salvarLog)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(dataEnvio, that.dataEnvio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, clienteId, mensagem, contexto, modelo, temperatura, maxTokens, salvarLog,
                tenantId, traceId, dataEnvio);
    }

    @Override
    public String toString() {
        return "MensagemIARequestDTO{" + "requestId=" + requestId + ", clienteId=" + clienteId + ", mensagem='"
                + mensagem + '\'' + ", contexto=" + contexto + ", modelo='" + modelo + '\'' + ", temperatura="
                + temperatura + ", maxTokens=" + maxTokens + ", salvarLog=" + salvarLog + ", tenantId='" + tenantId
                + '\'' + ", traceId=" + traceId + ", dataEnvio=" + dataEnvio + '}';
    }
}
