/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.config.AppAiOllamaProperties
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.adapters.outbound.ai;

import br.com.redemaisfarma.adapters.outbound.ai.ollama.OllamaChatClient;
import br.com.redemaisfarma.application.config.AppAiOllamaProperties;
import br.com.redemaisfarma.application.service.ai.AiAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OllamaAiAssistantService
implements AiAssistantService {
    private static final Logger log = LoggerFactory.getLogger(OllamaAiAssistantService.class);
    private final AppAiOllamaProperties props;
    private final OllamaChatClient client;

    public OllamaAiAssistantService(AppAiOllamaProperties props, OllamaChatClient client) {
        this.props = props;
        this.client = client;
    }

    @Override
    public String answer(String sessionId, String message) {
        if (!this.props.isEnabled()) {
            return this.fallback(message);
        }
        try {
            return this.client.chatOnce(sessionId, message);
        }
        catch (Exception e) {
            log.warn("Ollama falhou: {}", (Object)e.toString());
            return this.fallback(message);
        }
    }

    private String fallback(String message) {
        if (message == null || message.isBlank()) {
            return "Oi! Posso buscar produtos, calcular frete/prazo e consultar o status do pedido (#12345). Como posso ajudar?";
        }
        String m = message.toLowerCase();
        if (m.contains("pedido")) {
            return "Me informe o n\u00famero do pedido (ex: #12345) para consultar o status.";
        }
        if (m.contains("frete") || m.contains("prazo") || m.contains("entrega")) {
            return "Me envie seu CEP para calcular frete e prazo.";
        }
        if (m.contains("vitamina") || m.contains("dor") || m.contains("gripe")) {
            return "Posso sugerir itens do cat\u00e1logo. Prefere por pre\u00e7o, marca ou categoria?";
        }
        return "Entendi. Posso buscar produtos, ver status de pedido, calcular frete/prazo e disponibilidade em loja. O que voc\u00ea precisa?";
    }
}

