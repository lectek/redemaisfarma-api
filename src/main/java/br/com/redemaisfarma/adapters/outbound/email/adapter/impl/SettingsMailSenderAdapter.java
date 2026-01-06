package br.com.redemaisfarma.adapters.outbound.email.adapter.impl;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SettingsMailSenderAdapter implements MailSenderAdapter {
    private static final Logger log = LoggerFactory.getLogger(SettingsMailSenderAdapter.class);

    private static final String KEY_ENABLED = "email.enabled";
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

    public SettingsMailSenderAdapter(AppSettingService settings) {
        this.settings = settings;
    }

    @Override
    public String send(String to, String subject, String htmlBody, @Nullable List<String> bcc) {
        if (!settings.getBoolean(KEY_ENABLED, false)) {
            log.info("[mail] envio DESLIGADO (email.enabled=false). '{}' -> {}", subject, to);
            return "noop-disabled";
        }

        String host = settings.getOrDefault(KEY_SMTP_HOST, "").trim();
        if (host.isBlank()) {
            throw new IllegalStateException("SMTP host nao configurado.");
        }
        String fromEmail = settings.getOrDefault(KEY_FROM_EMAIL, "").trim();
        if (fromEmail.isBlank()) {
            throw new IllegalStateException("Email remetente nao configurado.");
        }

        JavaMailSenderImpl sender = buildSender(host);
        try {
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true);
            helper.setTo(to);
            setFrom(helper, fromEmail, settings.getOrDefault(KEY_FROM_NAME, "").trim());
            String replyTo = settings.getOrDefault(KEY_REPLY_TO, "").trim();
            if (!replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            msg.setSentDate(new Date());
            if (bcc != null && !bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(new String[0]));
            }
            sender.send(msg);
            return msg.getMessageID() != null ? msg.getMessageID() : "<none>";
        } catch (Exception ex) {
            log.error("[mail] falha ao enviar '{}'", subject, ex);
            throw new RuntimeException("Falha ao enviar e-mail", ex);
        }
    }

    private JavaMailSenderImpl buildSender(String host) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(readPort());
        sender.setUsername(settings.getOrDefault(KEY_SMTP_USER, "").trim());
        sender.setPassword(settings.getOrDefault(KEY_SMTP_PASS, "").trim());
        sender.setJavaMailProperties(buildProps());
        return sender;
    }

    private int readPort() {
        String raw = settings.getOrDefault(KEY_SMTP_PORT, "587").trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return 587;
        }
    }

    private Properties buildProps() {
        Properties props = new Properties();
        boolean tls = settings.getBoolean(KEY_SMTP_TLS, true);
        boolean ssl = settings.getBoolean(KEY_SMTP_SSL, false);
        String user = settings.getOrDefault(KEY_SMTP_USER, "").trim();
        String pass = settings.getOrDefault(KEY_SMTP_PASS, "").trim();
        boolean auth = !user.isBlank() || !pass.isBlank();
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        props.put("mail.smtp.ssl.enable", String.valueOf(ssl));
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return props;
    }

    private void setFrom(MimeMessageHelper helper, String email, String name) throws Exception {
        if (name != null && !name.isBlank()) {
            helper.setFrom(email, name);
        } else {
            helper.setFrom(email);
        }
    }
}
