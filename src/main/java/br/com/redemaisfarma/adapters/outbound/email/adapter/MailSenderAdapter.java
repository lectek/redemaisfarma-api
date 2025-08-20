package br.com.redemaisfarma.adapters.outbound.email.adapter;

import br.com.redemaisfarma.adapters.outbound.email.config.MailProperties;
import br.com.redemaisfarma.adapters.outbound.email.exception.MailSendException;
import br.com.redemaisfarma.adapters.outbound.email.model.EmailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Adapter concreto para envio de e-mails.
 */
public class MailSenderAdapter {

    private final JavaMailSender sender;
    private final MailProperties props;

    public MailSenderAdapter(JavaMailSender sender, MailProperties props) {
        this.sender = Objects.requireNonNull(sender);
        this.props = Objects.requireNonNull(props);
    }

    public void send(EmailMessage message) {
        try {
            MimeMessage mime = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());

            helper.setFrom(props.getDefaultFrom());
            helper.setTo(message.getTo().toArray(new String[0]));

            if (message.getCc() != null && !message.getCc().isEmpty())
                helper.setCc(message.getCc().toArray(new String[0]));

            if (message.getBcc() != null && !message.getBcc().isEmpty())
                helper.setBcc(message.getBcc().toArray(new String[0]));
            else if (props.getBcc() != null)
                helper.setBcc(props.getBcc().toArray(new String[0]));

            helper.setSubject(message.getSubject());

            if (message.getHtmlBody() != null) {
                helper.setText(message.getTextBody(), message.getHtmlBody());
            } else {
                helper.setText(message.getTextBody() != null ? message.getTextBody() : "");
            }

            sender.send(mime);
        } catch (Exception e) {
            throw new MailSendException("Falha ao enviar e-mail para: " + message.getTo(), e);
        }
    }
}
