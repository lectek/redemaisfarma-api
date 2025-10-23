// src/main/java/br/com/redemaisfarma/adapters/outbound/email/config/AppMailProperties.java
package br.com.redemaisfarma.adapters.outbound.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;

/**
 * Propriedades de e-mail da aplicação.
 *
 * Prefixo no YAML/ENV: {@code app.mail}
 *
 * Exemplo:
 * <pre>
 * app:
 *   mail:
 *     enabled: true
 *     from: "RedeMais Farma <no-reply@dev.redemaisfarma.local>"
 *     bcc: ["audit@exemplo.com", "ops@exemplo.com"]
 *     css-url: "https://cdn.exemplo.com/assets/mail/main.css"
 *     send-timeout-ms: 10000
 * </pre>
 *
 * Observações:
 * - Quando {@code enabled=true}, {@code from} é obrigatório (validado por {@link #isFromValidWhenEnabled()}).
 * - {@code bcc} aceita lista; variáveis de ambiente podem ser passadas como string separada por vírgulas.
 * - {@code cssUrl} é opcional (se quiser injetar um CSS público no HTML).
 * - {@code sendTimeoutMs} é um timeout lógico do seu adapter; para timeouts do JavaMail use as chaves
 *   {@code spring.mail.properties.mail.smtp.*}.
 */
@Validated
@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

    /** Liga/desliga envio real de e-mail pelo adapter. */
    private boolean enabled = false;

    /**
     * Remetente. Aceita "email@dominio" ou "Nome Completo &lt;email@dominio&gt;".
     * Obrigatório quando {@code enabled=true}.
     */
    private String from;

    /**
     * Lista de BCC globais (aplicados a todos os envios).
     * Mantida não nula para evitar NPE.
     */
    private List<String> bcc = new ArrayList<>();

    /** (Opcional) URL pública de CSS a ser embutido nos e-mails. */
    private String cssUrl;

    /**
     * Timeout lógico (em ms) usado pelo adapter para operações de envio.
     * Não substitui os timeouts do JavaMail.
     */
    private int sendTimeoutMs = 10000;

    // ===== Validações condicionais =====

    @AssertTrue(message = "app.mail.from deve ser informado quando app.mail.enabled=true")
    public boolean isFromValidWhenEnabled() {
        return !enabled || (from != null && !from.isBlank());
    }

    // ===== Getters/Setters =====

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public List<String> getBcc() { return bcc; }
    public void setBcc(List<String> bcc) { this.bcc = (bcc == null ? new ArrayList<>() : bcc); }

    public String getCssUrl() { return cssUrl; }
    public void setCssUrl(String cssUrl) { this.cssUrl = cssUrl; }

    public int getSendTimeoutMs() { return sendTimeoutMs; }
    public void setSendTimeoutMs(int sendTimeoutMs) { this.sendTimeoutMs = sendTimeoutMs; }

    // ===== Helpers opcionais =====

    /** Verdadeiro se houver pelo menos um BCC global configurado. */
    public boolean hasGlobalBcc() {
        return bcc != null && !bcc.isEmpty();
    }
}
