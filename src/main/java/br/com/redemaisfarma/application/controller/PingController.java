package br.com.redemaisfarma.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 🔄 Controller de Health Check da API Embalando.
 *
 * <p>
 * Esse endpoint serve para validações rápidas sobre a disponibilidade da API, sendo utilizado por sistemas de
 * monitoramento, ferramentas de deploy (como Docker ou Kubernetes), ou testes automatizados.
 * </p>
 *
 * <p>
 * Exemplo de uso:
 *
 * <pre>{@code
 * GET / api / ping
 * }</pre>
 *
 * Resposta: <code>"pong"</code>
 * </p>
 *
 * @author LekTec
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/ping")
public class PingController {

    /**
     * 🧪 Endpoint de verificação ("health check").
     *
     * @return ResponseEntity com texto "pong" e status HTTP 200.
     */
    @GetMapping
    public ResponseEntity<String> responderPing() {
        return ResponseEntity.ok("pong");
    }
}
