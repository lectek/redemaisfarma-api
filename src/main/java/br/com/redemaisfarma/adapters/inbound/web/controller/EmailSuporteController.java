/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.dto.request.EmailSuporteRequestDTO;
import br.com.redemaisfarma.application.service.SuporteMailService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/suporte"}, produces={"application/json"})
public class EmailSuporteController {
    private static final Logger log = LoggerFactory.getLogger(EmailSuporteController.class);
    private final SuporteMailService service;

    @PostMapping(value={"/email"}, consumes={"application/json"})
    public ResponseEntity<Map<String, Object>> enviar(@Valid @RequestBody EmailSuporteRequestDTO req) {
        UUID trace = req.getTraceId() != null ? req.getTraceId() : UUID.randomUUID();
        log.info("[suporte] recebida solicita\u00e7\u00e3o (tenantId={}, assunto={}, prioridade={}, categoria={}, traceId={})", new Object[]{req.getTenantId(), req.getAssunto(), req.getPrioridade(), req.getCategoria(), trace});
        this.service.enviar(req);
        return ResponseEntity.ok(Map.of("status", "sent", "assunto", req.getAssunto(), "prioridade", req.getPrioridade(), "categoria", req.getCategoria(), "tenantId", req.getTenantId(), "traceId", trace));
    }

    @GetMapping(value={"/ping"})
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok((Object)"suporte ok");
    }

    @Generated
    public EmailSuporteController(SuporteMailService service) {
        this.service = service;
    }
}

