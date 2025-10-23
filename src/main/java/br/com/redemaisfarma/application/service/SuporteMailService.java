// src/main/java/br/com/redemaisfarma/application/service/SuporteMailService.java
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.email.config.AppMailProperties;
import br.com.redemaisfarma.application.dto.request.EmailSuporteRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuporteMailService {

    private static final Logger log = LoggerFactory.getLogger(SuporteMailService.class);

    private final TemplateEngine templateEngine;
    private final MailSenderAdapter sender;
    private final AppMailProperties mailProps;

    /** Para onde o suporte recebe (configure em application-*.yml -> app.company.support-email) */
    private final String suporteEmail;

    /** Valores reais para o template (em vez de strings literais) */
    private final String companyName;
    private final String companySite;

    public SuporteMailService(
            TemplateEngine templateEngine,
            MailSenderAdapter sender,
            AppMailProperties mailProps,
            @Value("${app.company.support-email:no-reply@local.redemaisfarma}") String suporteEmail,
            @Value("${app.company.name:Rede Mais Farma}") String companyName,
            @Value("${app.company.site:https://local.redemaisfarma}") String companySite
    ) {
        this.templateEngine = templateEngine;
        this.sender = sender;
        this.mailProps = mailProps;
        this.suporteEmail = suporteEmail;
        this.companyName = companyName;
        this.companySite = companySite;
    }

    public void enviar(EmailSuporteRequestDTO req) {
        // Contexto Thymeleaf com valores resolvidos
        Context ctx = new Context();
        Map<String, Object> company = Map.of(
                "name", companyName,
                "site", companySite
        );
        ctx.setVariables(Map.of(
                "req", req,
                "cssUrl", mailProps.getCssUrl(),
                "company", company
        ));

        String html = templateEngine.process("pages/mail/suporte", ctx);

        // BCC: aceita múltiplos separados por vírgula
        List<String> bcc = Optional.ofNullable(req.getBcc())
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .filter(v -> !v.isBlank())
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);

        String assunto = "[SUPORTE] " + req.getCategoria() + " | " + req.getAssunto();

        try {
            log.info("[suporte] Enviando email (to={}, assunto={}, bcc={}, tenantId={}, traceId={})",
                    suporteEmail, assunto, bcc, req.getTenantId(), req.getTraceId());
            sender.send(suporteEmail, assunto, html, bcc);
            log.info("[suporte] Email enviado com sucesso (to={}, assunto={})", suporteEmail, assunto);
        } catch (Exception ex) {
            log.error("[suporte] Falha ao enviar email (to={}, assunto={}): {}", suporteEmail, assunto, ex.getMessage(), ex);
            throw ex;
        }
    }
}
