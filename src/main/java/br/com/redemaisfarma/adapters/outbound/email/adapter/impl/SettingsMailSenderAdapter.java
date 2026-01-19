package br.com.redemaisfarma.adapters.outbound.email.adapter.impl;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
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
    private final Environment env;

    public SettingsMailSenderAdapter(AppSettingService settings, Environment env) {
        this.settings = settings;
        this.env = env;
    }

    @Override
    public String send(String to, String subject, String htmlBody, @Nullable List<String> bcc) {
        if (!isEnabled()) {
            log.info("[mail] envio DESLIGADO (email.enabled=false). '{}' -> {}", subject, to);
            return "noop-disabled";
        }

        String host = getOrDefault(KEY_SMTP_HOST, "SPRING_MAIL_HOST", "").trim();
        if (host.isBlank()) {
            throw new IllegalStateException("SMTP host nao configurado.");
        }
        String fromEmail = getOrDefault(KEY_FROM_EMAIL, "APP_MAIL_FROM", "").trim();
        if (fromEmail.isBlank()) {
            throw new IllegalStateException("Email remetente nao configurado.");
        }

        JavaMailSenderImpl sender = buildSender(host);
        try {
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true);
            helper.setTo(to);
            setFrom(helper, fromEmail, getOrDefault(KEY_FROM_NAME, "APP_MAIL_FROM_NAME", "").trim());
            String replyTo = safeTrim(settings.getOrDefault(KEY_REPLY_TO, ""));
            String replyToEnv = env.getProperty("APP_MAIL_REPLY_TO");
            if (replyToEnv != null && !replyToEnv.isBlank() && replyTo.isBlank()) {
                replyTo = replyToEnv.trim();
            }
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
        sender.setUsername(getOrDefault(KEY_SMTP_USER, "SPRING_MAIL_USERNAME", "").trim());
        sender.setPassword(getOrDefault(KEY_SMTP_PASS, "SPRING_MAIL_PASSWORD", "").trim());
        sender.setJavaMailProperties(buildProps());
        return sender;
    }

    private int readPort() {
        String raw = getOrDefault(KEY_SMTP_PORT, "SPRING_MAIL_PORT", "587").trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return 587;
        }
    }

    private Properties buildProps() {
        Properties props = new Properties();
        boolean tls = getBooleanOrEnv(KEY_SMTP_TLS, "SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE", true);
        boolean ssl = getBooleanOrEnv(KEY_SMTP_SSL, "SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE", false);
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

    private boolean isEnabled() {
        return getBooleanOrEnv(KEY_ENABLED, "APP_MAIL_ENABLED", false);
    }

    private boolean getBooleanOrEnv(String key, String envKey, boolean defaultValue) {
        return settings.getBoolean(key, boolEnv(envKey, defaultValue));
    }

    private boolean boolEnv(String envKey, boolean defaultValue) {
        String raw = env.getProperty(envKey);
        if (raw == null) {
            return defaultValue;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("yes") || normalized.equals("on") || normalized.equals("verdade") || normalized.equals("sim");
    }

    private static String safeTrim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String getOrDefault(String key, String envKey, String defaultValue) {
        String value = settings.getOrDefault(key, "").trim();
        if (!value.isBlank()) {
            return value;
        }
        String envValue = env.getProperty(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        return defaultValue;
    }
}
