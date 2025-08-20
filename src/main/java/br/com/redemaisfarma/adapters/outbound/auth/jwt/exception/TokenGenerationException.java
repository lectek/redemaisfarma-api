package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

/**
 * Lançada quando ocorre erro na geração de um JWT (access ou refresh).
 */
public class TokenGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenGenerationException(String message) {
        super(message);
    }

    public TokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
