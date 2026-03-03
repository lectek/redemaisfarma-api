package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/email")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesEmailController {

    /**
     * Settings key for email module enablement.
     */
    private static final String KEY_EMAIL_ENABLED = "email.enabled";

    /**
     * Settings key for SMTP host.
     */
    private static final String KEY_SMTP_HOST = "email.smtp_host";

    /**
     * Settings key for SMTP port.
     */
    private static final String KEY_SMTP_PORT = "email.smtp_port";

    /**
     * Settings key for SMTP username.
     */
    private static final String KEY_SMTP_USER = "email.smtp_user";

    /**
     * Settings key for SMTP password.
     */
    private static final String KEY_SMTP_PASS = "email.smtp_pass";

    /**
     * Settings key for SMTP TLS flag.
     */
    private static final String KEY_SMTP_TLS = "email.smtp_tls";

    /**
     * Settings key for SMTP SSL flag.
     */
    private static final String KEY_SMTP_SSL = "email.smtp_ssl";

    /**
     * Settings key for sender email.
     */
    private static final String KEY_FROM_EMAIL = "email.from_email";

    /**
     * Settings key for sender name.
     */
    private static final String KEY_FROM_NAME = "email.from_name";

    /**
     * Settings key for reply-to address.
     */
    private static final String KEY_REPLY_TO = "email.reply_to";

    /**
     * Service that stores app settings.
     */
    private final AppSettingService settings;

    /**
     * Creates controller with settings service dependency.
     *
     * @param service settings service
     */
    public AdminConfiguracoesEmailController(final AppSettingService service) {
        this.settings = service;
    }

    /**
     * Renders email settings form.
     *
     * @param model view model
     * @return email settings page
     */
    @GetMapping
    public String form(final Model model) {
        final EmailForm form = new EmailForm();
        form.setEnabled(settings.getBoolean(KEY_EMAIL_ENABLED, false));
        form.setSmtpHost(settings.getOrDefault(KEY_SMTP_HOST, ""));
        form.setSmtpPort(settings.getOrDefault(KEY_SMTP_PORT, "587"));
        form.setSmtpUser(settings.getOrDefault(KEY_SMTP_USER, ""));
        form.setSmtpPass(settings.getOrDefault(KEY_SMTP_PASS, ""));
        form.setSmtpTls(settings.getBoolean(KEY_SMTP_TLS, true));
        form.setSmtpSsl(settings.getBoolean(KEY_SMTP_SSL, false));
        form.setFromEmail(settings.getOrDefault(KEY_FROM_EMAIL, ""));
        form.setFromName(settings.getOrDefault(KEY_FROM_NAME, ""));
        form.setReplyTo(settings.getOrDefault(KEY_REPLY_TO, ""));

        model.addAttribute("cfg", form);
        return "pages/admin/configuracoes/email";
    }

    /**
     * Persists email settings.
     *
     * @param form form payload
     * @param ra redirect attributes
     * @return redirect to email settings page
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("cfg") final EmailForm form,
            final RedirectAttributes ra
    ) {
        settings.upsert(
                KEY_EMAIL_ENABLED,
                bool(form.getEnabled()),
                "Email ativo"
        );
        settings.upsert(
                KEY_SMTP_HOST,
                nullSafe(form.getSmtpHost()),
                "SMTP host"
        );
        settings.upsert(
                KEY_SMTP_PORT,
                nullSafe(form.getSmtpPort()),
                "SMTP port"
        );
        settings.upsert(
                KEY_SMTP_USER,
                nullSafe(form.getSmtpUser()),
                "SMTP usuario"
        );
        settings.upsert(
                KEY_SMTP_PASS,
                nullSafe(form.getSmtpPass()),
                "SMTP senha"
        );
        settings.upsert(KEY_SMTP_TLS, bool(form.getSmtpTls()), "SMTP TLS");
        settings.upsert(KEY_SMTP_SSL, bool(form.getSmtpSsl()), "SMTP SSL");
        settings.upsert(
                KEY_FROM_EMAIL,
                nullSafe(form.getFromEmail()),
                "Email remetente"
        );
        settings.upsert(
                KEY_FROM_NAME,
                nullSafe(form.getFromName()),
                "Nome do remetente"
        );
        settings.upsert(KEY_REPLY_TO, nullSafe(form.getReplyTo()), "Reply-to");

        ra.addFlashAttribute("success", "Configuracoes de email atualizadas.");
        return "redirect:/admin/configuracoes/email";
    }

    private static String nullSafe(final String value) {
        return value == null ? "" : value.trim();
    }

    private static String bool(final Boolean value) {
        return Boolean.TRUE.equals(value) ? "true" : "false";
    }

    /**
     * Form payload for email settings page.
     */
    @Getter
    @Setter
    public static final class EmailForm {

        /**
         * Whether email module is enabled.
         */
        private Boolean enabled;

        /**
         * SMTP host.
         */
        private String smtpHost;

        /**
         * SMTP port.
         */
        private String smtpPort;

        /**
         * SMTP username.
         */
        private String smtpUser;

        /**
         * SMTP password.
         */
        private String smtpPass;

        /**
         * SMTP TLS flag.
         */
        private Boolean smtpTls;

        /**
         * SMTP SSL flag.
         */
        private Boolean smtpSsl;

        /**
         * Sender email.
         */
        private String fromEmail;

        /**
         * Sender name.
         */
        private String fromName;

        /**
         * Reply-to address.
         */
        private String replyTo;
    }
}
