package br.com.redemaisfarma.adapters.outbound.http.exception;

import java.util.List;
import java.util.Map;

/**
 * Falha ao desserializar o corpo de resposta do serviço externo.
 */
public class DeserializationException extends ExternalClientException {
    private static final long serialVersionUID = 1L;

    public DeserializationException(String message, String service, String method, String url, Integer status,
            String responseBody, Map<String, List<String>> headers, String traceId, Throwable cause) {
        super(message, service, method, url, status, responseBody, headers, traceId, cause);
    }
}
