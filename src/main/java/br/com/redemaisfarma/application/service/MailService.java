package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final MailSenderAdapter adapter;
    private final SpringTemplateEngine templateEngine;

    /** Envia HTML (preferencial). */
    public void sendHtml(String to, String subject, String htmlBody, @Nullable List<String> bcc) {
        log.debug("[mail] preparando envio HTML: '{}' -> {}", subject, to);
        adapter.send(to, subject, htmlBody, bcc);
    }

    /** Conveniência: recebe texto puro e encapsula em HTML básico. */
    public void sendText(String to, String subject, String text, @Nullable List<String> bcc) {
        String html = "<pre style=\"white-space:pre-wrap;font-family:inherit;\">"
                + HtmlUtils.htmlEscape(text)
                + "</pre>";
        sendHtml(to, subject, html, bcc);
    }

    /** Processa um template Thymeleaf (ex.: 'email/test') com o model. */
    public void sendTemplate(String to, String subject, String template, Map<String, Object> model, @Nullable List<String> bcc) {
        Context ctx = new Context();
        if (model != null) model.forEach(ctx::setVariable);
        String html = templateEngine.process(template, ctx);
        sendHtml(to, subject, html, bcc);
    }
}
