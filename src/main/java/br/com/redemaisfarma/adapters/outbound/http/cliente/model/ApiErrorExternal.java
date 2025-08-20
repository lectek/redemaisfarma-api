package br.com.redemaisfarma.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Modelo para mapear erros padronizados que a API externa possa retornar. Útil para enriquecer logs e traduzir para
 * exceções da sua camada application.
 */
public class ApiErrorExternal implements Serializable {

    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("path")
    private String path;

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
