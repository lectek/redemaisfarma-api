package br.com.redemaisfarma.adapters.outbound.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Propriedades específicas de envio de e-mail para o módulo outbound/email.
 */
@Validated
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /**
     * Endereço padrão do remetente.
     */
    @NotBlank
    private String defaultFrom;

    /**
     * Lista opcional de e-mails para BCC automático.
     */
    private List<String> bcc;

    /**
     * Timeout de envio em milissegundos.
     */
    private int sendTimeoutMs = 10000;

    public String getDefaultFrom() {
        return defaultFrom;
    }

    public void setDefaultFrom(String defaultFrom) {
        this.defaultFrom = defaultFrom;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public int getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    public void setSendTimeoutMs(int sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
    }
}
