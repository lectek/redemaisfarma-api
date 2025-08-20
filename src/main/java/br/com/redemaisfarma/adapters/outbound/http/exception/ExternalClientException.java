package br.com.redemaisfarma.adapters.outbound.http.exception;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Exceção base para falhas em chamadas HTTP a serviços externos. Carrega metadados úteis para logs/observabilidade.
 */
public class ExternalClientException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String service; // identificação do serviço externo (ex.: "cliente")
    private final String method; // GET/POST/PUT/DELETE…
    private final String url; // URL final (com pathvars e query)
    private final Integer status; // HTTP status (se houver)
    private final String responseBody; // corpo retornado (se houver)
    private final Map<String, List<String>> headers; // headers de resposta (se houver)
    private final String traceId; // trace/correlation id opcional

    public ExternalClientException(String message, String service, String method, String url, Integer status,
            String responseBody, Map<String, List<String>> headers, String traceId, Throwable cause) {
        super(message, cause);
        this.service = service;
        this.method = method;
        this.url = url;
        this.status = status;
        this.responseBody = responseBody;
        this.headers = headers;
        this.traceId = traceId;
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Integer getStatus() {
        return status;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getTraceId() {
        return traceId;
    }

    @Override
    public String toString() {
        return "ExternalClientException{" + "service='" + service + '\'' + ", method='" + method + '\'' + ", url='"
                + url + '\'' + ", status=" + status + ", traceId='" + traceId + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExternalClientException))
            return false;
        ExternalClientException that = (ExternalClientException) o;
        return Objects.equals(service, that.service) && Objects.equals(method, that.method)
                && Objects.equals(url, that.url) && Objects.equals(status, that.status)
                && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, method, url, status, traceId);
    }
}
