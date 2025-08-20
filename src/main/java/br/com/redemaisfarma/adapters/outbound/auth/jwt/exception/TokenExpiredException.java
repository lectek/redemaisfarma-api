package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

/** Lançada quando o JWT já expirou. */
public class TokenExpiredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
