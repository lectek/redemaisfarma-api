package br.com.redemaisfarma.adapters.outbound.http.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 7807 – Problem Details para erros HTTP de serviços externos. Mantém campos opcionais e "extensions" para dados
 * extras do provedor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    /** URI de identificação do tipo de problema (pode ser uma doc). */
    @JsonProperty("type")
    private String type;

    /** Título legível do problema. */
    @JsonProperty("title")
    private String title;

    /** Código HTTP retornado pelo serviço externo. */
    @JsonProperty("status")
    private Integer status;

    /** Descrição detalhada (geralmente em linguagem natural). */
    @JsonProperty("detail")
    private String detail;

    /** Instância/ocorrência específica (URI para log/tracing). */
    @JsonProperty("instance")
    private String instance;

    /** Momento em que o erro foi gerado (se o provedor enviar). */
    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;

    /** Campos adicionais do provedor (ex.: "errors", "traceId", etc.). */
    @JsonProperty("extensions")
    private Map<String, Object> extensions;

    public ProblemDetails() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProblemDetails))
            return false;
        ProblemDetails that = (ProblemDetails) o;
        return Objects.equals(type, that.type) && Objects.equals(title, that.title)
                && Objects.equals(status, that.status) && Objects.equals(detail, that.detail)
                && Objects.equals(instance, that.instance) && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(extensions, that.extensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, status, detail, instance, timestamp, extensions);
    }

    @Override
    public String toString() {
        return "ProblemDetails{" + "type='" + type + '\'' + ", title='" + title + '\'' + ", status=" + status
                + ", detail='" + detail + '\'' + ", instance='" + instance + '\'' + ", timestamp=" + timestamp
                + ", extensions=" + extensions + '}';
    }
}
