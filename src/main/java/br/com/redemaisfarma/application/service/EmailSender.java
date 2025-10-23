package br.com.redemaisfarma.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class EmailSender {

    private final JavaMailSender mail;

    public EmailSender(JavaMailSender mail) {
        this.mail = mail;
    }

    /**
     * Envia e-mail HTML. Retorna o Message-ID (ou UUID se não disponível).
     */
    public String sendHtml(String to, String from, String subject, String html) {
        try {
            MimeMessage msg = mail.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            h.setTo(to);
            h.setFrom(from);
            h.setSubject(subject);
            h.setText(html, true);
            mail.send(msg);

            String[] hdr = msg.getHeader("Message-ID");
            return (hdr != null && hdr.length > 0) ? hdr[0] : UUID.randomUUID().toString();
        } catch (MessagingException ex) {
            throw new RuntimeException("mail_send_failed: " + ex.getMessage(), ex);
        }
    }
}
