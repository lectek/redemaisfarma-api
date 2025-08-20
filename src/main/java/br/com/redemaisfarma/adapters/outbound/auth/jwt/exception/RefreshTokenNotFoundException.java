package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

/** Lançada quando o refresh token não é encontrado no store/persistência. */
public class RefreshTokenNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }

    public RefreshTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
