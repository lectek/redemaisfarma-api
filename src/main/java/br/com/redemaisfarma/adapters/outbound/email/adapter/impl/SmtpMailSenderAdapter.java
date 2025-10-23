package br.com.redemaisfarma.adapters.outbound.email.adapter.impl;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.email.config.AppMailProperties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class SmtpMailSenderAdapter implements MailSenderAdapter {

  private static final Logger log = LoggerFactory.getLogger(SmtpMailSenderAdapter.class);
  private final JavaMailSender mailSender;
  private final AppMailProperties props;

  public SmtpMailSenderAdapter(JavaMailSender mailSender, AppMailProperties props) {
    this.mailSender = mailSender;
    this.props = props;
  }

  @Override
  public String send(String to, String subject, String htmlBody, @Nullable List<String> bcc) {
    if (!props.isEnabled()) {
      log.info("[mail] envio DESLIGADO (app.mail.enabled=false). '{}' -> {}", subject, to);
      return "noop-disabled";
    }
    try {
      MimeMessage msg = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(
          msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
      helper.setValidateAddresses(true);

      helper.setTo(to);
      setFromSmart(helper, props.getFrom());
      helper.setSubject(subject);
      helper.setText(htmlBody, true);
      msg.setSentDate(new Date());

      List<String> allBcc = new ArrayList<>();
      if (bcc != null && !bcc.isEmpty()) allBcc.addAll(bcc);
      if (props.getBcc() != null && !props.getBcc().isEmpty()) allBcc.addAll(props.getBcc());
      if (!allBcc.isEmpty()) helper.setBcc(allBcc.toArray(new String[0]));

      mailSender.send(msg);
      return msg.getMessageID() != null ? msg.getMessageID() : "<none>";
    } catch (Exception e) {
      log.error("[mail] falha ao enviar '{}'", subject, e);
      throw new RuntimeException("Falha ao enviar e-mail", e);
    }
  }

  private void setFromSmart(MimeMessageHelper helper, String fromRaw) {
    try {
      InternetAddress[] parsed = InternetAddress.parse(fromRaw, true);
      if (parsed.length > 0) {
        InternetAddress ia = parsed[0];
        if (ia.getPersonal() != null && !ia.getPersonal().isBlank()) {
          helper.setFrom(ia.getAddress(), ia.getPersonal());
        } else {
          helper.setFrom(ia.getAddress());
        }
        return;
      }
    } catch (Exception ignore) {}
    try { helper.setFrom(fromRaw); } catch (Exception ignore) {}
  }
}
