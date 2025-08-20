package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

/** Lançada quando o token foi revogado no store. */
public class TokenRevokedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenRevokedException(String message) {
        super(message);
    }

    public TokenRevokedException(String message, Throwable cause) {
        super(message, cause);
    }
}
