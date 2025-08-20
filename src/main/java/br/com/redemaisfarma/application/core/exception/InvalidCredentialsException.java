package br.com.redemaisfarma.application.core.exception;

/**
 * Exceção usada para indicar falhas de autenticação por credenciais inválidas. Pode ser capturada para exibir mensagens
 * amigáveis ao usuário ou registrar tentativas no log.
 */
public class InvalidCredentialsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Credenciais inválidas. Verifique e tente novamente.";

    /**
     * Construtor padrão com mensagem genérica.
     */
    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Construtor com mensagem personalizada.
     *
     * @param message
     *            mensagem de erro específica
     */
    public InvalidCredentialsException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
    }

    /**
     * Construtor com mensagem e causa.
     *
     * @param message
     *            mensagem de erro específica
     * @param cause
     *            exceção de origem
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, cause);
    }

    /**
     * Construtor apenas com a causa (por exemplo, exceções internas de banco).
     *
     * @param cause
     *            exceção de origem
     */
    public InvalidCredentialsException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
}
