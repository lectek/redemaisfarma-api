package br.com.redemaisfarma.adapters.outbound.http.exception;

/**
 * Timeout do CLIENTE (ex.: connect/read timeout do WebClient), onde não há status HTTP de resposta.
 */
public class ClientTimeoutException extends ExternalClientException {
    private static final long serialVersionUID = 1L;

    public ClientTimeoutException(String message, String service, String method, String url, String traceId,
            Throwable cause) {
        super(message, service, method, url, null, null, null, traceId, cause);
    }
}
