package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

/**
 * Lançada quando ocorre erro de validação de um JWT.
 */
public class TokenValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenValidationException(String message) {
        super(message);
    }

    public TokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
