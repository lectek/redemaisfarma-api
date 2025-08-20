package br.com.redemaisfarma.application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 🌐 Endpoint de verificação rápida da API.
 *
 * Pode ser usado para:
 * <ul>
 * <li>Testes de disponibilidade (ping)</li>
 * <li>Integrações com Load Balancers</li>
 * <li>Health Checks em ambientes como Docker, Kubernetes, Railway etc.</li>
 * </ul>
 *
 * <p>
 * URL: <code>/api/status</code>
 * </p>
 *
 * @author LekTec
 *
 * @since 1.0
 */
@RestController
public class HelloController {

    /**
     * Retorna uma mensagem simples indicando que a API está funcionando.
     *
     * @return Mensagem "API Online ✅"
     */
    @GetMapping("/api/status")
    public String exibirStatusDaAPI() {
        return "✅ API Online";
    }
}
