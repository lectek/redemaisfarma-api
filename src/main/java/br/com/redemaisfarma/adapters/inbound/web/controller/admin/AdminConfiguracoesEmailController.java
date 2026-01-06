package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
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

    private static final String KEY_EMAIL_ENABLED = "email.enabled";
    private static final String KEY_SMTP_HOST = "email.smtp_host";
    private static final String KEY_SMTP_PORT = "email.smtp_port";
    private static final String KEY_SMTP_USER = "email.smtp_user";
    private static final String KEY_SMTP_PASS = "email.smtp_pass";
    private static final String KEY_SMTP_TLS = "email.smtp_tls";
    private static final String KEY_SMTP_SSL = "email.smtp_ssl";
    private static final String KEY_FROM_EMAIL = "email.from_email";
    private static final String KEY_FROM_NAME = "email.from_name";
    private static final String KEY_REPLY_TO = "email.reply_to";

    private final AppSettingService settings;

    public AdminConfiguracoesEmailController(AppSettingService settings) {
        this.settings = settings;
    }

    @GetMapping
    public String form(Model model) {
        EmailForm form = new EmailForm();
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

    @PostMapping
    public String salvar(@ModelAttribute("cfg") EmailForm form, RedirectAttributes ra) {
        settings.upsert(KEY_EMAIL_ENABLED, bool(form.getEnabled()), "Email ativo");
        settings.upsert(KEY_SMTP_HOST, nullSafe(form.getSmtpHost()), "SMTP host");
        settings.upsert(KEY_SMTP_PORT, nullSafe(form.getSmtpPort()), "SMTP port");
        settings.upsert(KEY_SMTP_USER, nullSafe(form.getSmtpUser()), "SMTP usuario");
        settings.upsert(KEY_SMTP_PASS, nullSafe(form.getSmtpPass()), "SMTP senha");
        settings.upsert(KEY_SMTP_TLS, bool(form.getSmtpTls()), "SMTP TLS");
        settings.upsert(KEY_SMTP_SSL, bool(form.getSmtpSsl()), "SMTP SSL");
        settings.upsert(KEY_FROM_EMAIL, nullSafe(form.getFromEmail()), "Email remetente");
        settings.upsert(KEY_FROM_NAME, nullSafe(form.getFromName()), "Nome do remetente");
        settings.upsert(KEY_REPLY_TO, nullSafe(form.getReplyTo()), "Reply-to");
        ra.addFlashAttribute("success", "Configuracoes de email atualizadas.");
        return "redirect:/admin/configuracoes/email";
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String bool(Boolean value) {
        return Boolean.TRUE.equals(value) ? "true" : "false";
    }

    public static class EmailForm {
        private Boolean enabled;
        private String smtpHost;
        private String smtpPort;
        private String smtpUser;
        private String smtpPass;
        private Boolean smtpTls;
        private Boolean smtpSsl;
        private String fromEmail;
        private String fromName;
        private String replyTo;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getSmtpHost() {
            return smtpHost;
        }

        public void setSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        public String getSmtpPort() {
            return smtpPort;
        }

        public void setSmtpPort(String smtpPort) {
            this.smtpPort = smtpPort;
        }

        public String getSmtpUser() {
            return smtpUser;
        }

        public void setSmtpUser(String smtpUser) {
            this.smtpUser = smtpUser;
        }

        public String getSmtpPass() {
            return smtpPass;
        }

        public void setSmtpPass(String smtpPass) {
            this.smtpPass = smtpPass;
        }

        public Boolean getSmtpTls() {
            return smtpTls;
        }

        public void setSmtpTls(Boolean smtpTls) {
            this.smtpTls = smtpTls;
        }

        public Boolean getSmtpSsl() {
            return smtpSsl;
        }

        public void setSmtpSsl(Boolean smtpSsl) {
            this.smtpSsl = smtpSsl;
        }

        public String getFromEmail() {
            return fromEmail;
        }

        public void setFromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }
    }
}
