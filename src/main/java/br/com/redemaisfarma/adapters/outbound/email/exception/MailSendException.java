package br.com.redemaisfarma.adapters.outbound.email.exception;

/**
 * Exceção específica para falhas no envio de e-mails.
 */
public class MailSendException extends RuntimeException {
    public MailSendException(String message) {
        super(message);
    }

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
