package br.com.redemaisfarma.application.core.exception;

public class UserBlockedException extends RuntimeException {
    public UserBlockedException() {
        super("Usuário temporariamente bloqueado por tentativas de login inválidas.");
    }

    public UserBlockedException(String message) {
        super(message);
    }
}
