package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.dto.request.EmailSuporteRequestDTO;
import br.com.redemaisfarma.application.service.SuporteMailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/suporte", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmailSuporteController {

    private final SuporteMailService service;

    @PostMapping(value = "/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> enviar(@Valid @RequestBody EmailSuporteRequestDTO req) {
        UUID trace = req.getTraceId() != null ? req.getTraceId() : UUID.randomUUID();
        // opcional: fixar o traceId no DTO para a cadeia de logs
        if (req.getTraceId() == null) req.setTraceId(trace);

        log.info("[suporte] recebida solicitação | tenantId={}, assunto='{}', prioridade={}, categoria={}, traceId={}",
                req.getTenantId(), req.getAssunto(), req.getPrioridade(), req.getCategoria(), trace);

        service.enviar(req);

        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "assunto", req.getAssunto(),
                "prioridade", String.valueOf(req.getPrioridade()),
                "categoria", String.valueOf(req.getCategoria()),
                "tenantId", req.getTenantId(),
                "traceId", trace.toString()
        ));
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("suporte ok");
    }
}
