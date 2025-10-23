package br.com.redemaisfarma.adapters.outbound.ai;

import br.com.redemaisfarma.adapters.outbound.ai.ollama.OllamaChatClient;
import br.com.redemaisfarma.application.config.AppAiOllamaProperties;
import br.com.redemaisfarma.application.service.ai.AiAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OllamaAiAssistantService implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(OllamaAiAssistantService.class);

    private final AppAiOllamaProperties props;
    private final OllamaChatClient client;

    public OllamaAiAssistantService(AppAiOllamaProperties props, OllamaChatClient client) {
        this.props = props;
        this.client = client;
    }

    @Override
    public String answer(String sessionId, String message) {
        if (!props.isEnabled()) {
            return fallback(message);
        }
        try {
            return client.chatOnce(sessionId, message);
        } catch (Exception e) {
            log.warn("Ollama falhou: {}", e.toString());
            return fallback(message);
        }
    }

    private String fallback(String message) {
        // fallback simples — mantém utilidade mesmo sem modelo
        if (message == null || message.isBlank()) {
            return "Oi! Posso buscar produtos, calcular frete/prazo e consultar o status do pedido (#12345). Como posso ajudar?";
        }
        String m = message.toLowerCase();
        if (m.contains("pedido")) return "Me informe o número do pedido (ex: #12345) para consultar o status.";
        if (m.contains("frete") || m.contains("prazo") || m.contains("entrega")) return "Me envie seu CEP para calcular frete e prazo.";
        if (m.contains("vitamina") || m.contains("dor") || m.contains("gripe")) return "Posso sugerir itens do catálogo. Prefere por preço, marca ou categoria?";
        return "Entendi. Posso buscar produtos, ver status de pedido, calcular frete/prazo e disponibilidade em loja. O que você precisa?";
    }
}
