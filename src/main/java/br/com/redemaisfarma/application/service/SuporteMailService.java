/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.dto.request.EmailSuporteRequestDTO
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.stereotype.Service
 *  org.thymeleaf.TemplateEngine
 *  org.thymeleaf.context.Context
 *  org.thymeleaf.context.IContext
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.email.config.AppMailProperties;
import br.com.redemaisfarma.application.dto.request.EmailSuporteRequestDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

@Service
public class SuporteMailService {
    private static final Logger log = LoggerFactory.getLogger(SuporteMailService.class);
    private final TemplateEngine templateEngine;
    private final MailSenderAdapter sender;
    private final AppMailProperties mailProps;
    private final String suporteEmail;
    private final String companyName;
    private final String companySite;

    public SuporteMailService(TemplateEngine templateEngine, MailSenderAdapter sender, AppMailProperties mailProps, @Value(value="${app.company.support-email:no-reply@local.redemaisfarma}") String suporteEmail, @Value(value="${app.company.name:Rede Mais Farma}") String companyName, @Value(value="${app.company.site:https://local.redemaisfarma}") String companySite) {
        this.templateEngine = templateEngine;
        this.sender = sender;
        this.mailProps = mailProps;
        this.suporteEmail = suporteEmail;
        this.companyName = companyName;
        this.companySite = companySite;
    }

    public void enviar(EmailSuporteRequestDTO req) {
        Context ctx = new Context();
        Map<String, String> company = Map.of("name", this.companyName, "site", this.companySite);
        ctx.setVariables(Map.of("req", req, "cssUrl", this.mailProps.getCssUrl(), "company", company));
        String html = this.templateEngine.process("pages/mail/suporte", (IContext)ctx);
        List bcc = Optional.ofNullable(req.getBcc()).map(s -> Arrays.stream(s.split(",")).map(String::trim).filter(v -> !v.isBlank()).collect(Collectors.toList())).orElseGet(Collections::emptyList);
        String assunto = "[SUPORTE] " + String.valueOf(req.getCategoria()) + " | " + req.getAssunto();
        try {
            log.info("[suporte] Enviando email (to={}, assunto={}, bcc={}, tenantId={}, traceId={})", new Object[]{this.suporteEmail, assunto, bcc, req.getTenantId(), req.getTraceId()});
            this.sender.send(this.suporteEmail, assunto, html, bcc);
            log.info("[suporte] Email enviado com sucesso (to={}, assunto={})", (Object)this.suporteEmail, (Object)assunto);
        }
        catch (Exception ex) {
            log.error("[suporte] Falha ao enviar email (to={}, assunto={}): {}", new Object[]{this.suporteEmail, assunto, ex.getMessage(), ex});
            throw ex;
        }
    }
}

