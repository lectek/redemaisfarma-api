package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

/** Token com formato malformado (compact serialization inválida). */
public class MalformedTokenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MalformedTokenException(String message) {
        super(message);
    }

    public MalformedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
