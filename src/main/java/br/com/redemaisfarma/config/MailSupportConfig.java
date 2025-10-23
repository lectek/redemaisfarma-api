package br.com.redemaisfarma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(prefix = "app.mail", name = "noop", havingValue = "true")
public class MailSupportConfig {

  @Bean
  @ConditionalOnMissingBean(JavaMailSender.class)
  public JavaMailSender noOpJavaMailSender() {
    return new NoOpJavaMailSender();
  }

  static class NoOpJavaMailSender implements JavaMailSender {
    private static final Logger log = LoggerFactory.getLogger(NoOpJavaMailSender.class);

    @Override @NonNull
    public MimeMessage createMimeMessage() {
      return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Override @NonNull
    public MimeMessage createMimeMessage(@NonNull InputStream contentStream) throws MailException {
      try {
        return new MimeMessage(Session.getInstance(new Properties()), contentStream);
      } catch (Exception e) {
        throw new MailException("Falha ao criar MimeMessage (no-op) a partir do InputStream", e) {
          private static final long serialVersionUID = 1L;
        };
      }
    }

    @Override
    public void send(@NonNull MimeMessage mimeMessage) throws MailException {
      log.debug("[MAIL NO-OP] MimeMessage suprimida (teste/dev). Assunto: {}", safeSubject(mimeMessage));
    }

    @Override
    public void send(@NonNull MimeMessage... mimeMessages) throws MailException {
      for (MimeMessage m : mimeMessages) send(m);
    }

    @Override
    public void send(@NonNull SimpleMailMessage simpleMessage) throws MailException {
      log.debug("[MAIL NO-OP] SimpleMailMessage suprimida (teste/dev). Assunto: {}",
          (simpleMessage != null ? simpleMessage.getSubject() : "<null>"));
    }

    @Override
    public void send(@NonNull SimpleMailMessage... simpleMessages) throws MailException {
      for (SimpleMailMessage m : simpleMessages) send(m);
    }

    private String safeSubject(@Nullable MimeMessage msg) {
      if (msg == null) return "<null>";
      try { return msg.getSubject(); } catch (Exception e) { return "<desconhecido>"; }
    }
  }
}
