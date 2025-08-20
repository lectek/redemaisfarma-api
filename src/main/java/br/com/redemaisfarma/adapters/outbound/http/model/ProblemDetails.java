package br.com.redemaisfarma.adapters.outbound.http.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Estrutura de erro inspirada na RFC 7807 (application/problem+json). Útil para padronizar tratamento de erros vindos
 * de integrações HTTP.
 */
public class ProblemDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type; // URI do tipo de problema
    private String title; // resumo legível
    private int status; // HTTP status
    private String detail; // descrição detalhada
    private String instance; // URI da ocorrência
    private OffsetDateTime timestamp; // quando ocorreu
    private Map<String, Object> extras; // campos adicionais

    public ProblemDetails() {
    }

    public ProblemDetails(String type, String title, int status, String detail, String instance,
            OffsetDateTime timestamp, Map<String, Object> extras) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.timestamp = timestamp;
        this.extras = extras;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
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

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProblemDetails))
            return false;
        ProblemDetails that = (ProblemDetails) o;
        return status == that.status && Objects.equals(type, that.type) && Objects.equals(title, that.title)
                && Objects.equals(detail, that.detail) && Objects.equals(instance, that.instance)
                && Objects.equals(timestamp, that.timestamp) && Objects.equals(extras, that.extras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, status, detail, instance, timestamp, extras);
    }

    @Override
    public String toString() {
        return "ProblemDetails{status=" + status + ", title='" + title + "', detail='" + detail + "'}";
    }
}
