package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

/** Lançada quando o JWT (access/refresh) é inválido (assinatura, claims, etc.). */
public class InvalidJwtTokenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidJwtTokenException(String message) {
        super(message);
    }

    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
