package br.com.redemaisfarma.application.service.ai;

/**
 * Porta de serviço para o assistente de IA usado no front do cliente (/ia).
 * Diferentes implementações podem ser plugadas (ex.: Ollama local, OpenRouter, etc.).
 */
public interface AiAssistantService {

    /**
     * Gera uma resposta para a mensagem do usuário.
     *
     * @param sessionId  identificador lógico da sessão/conversa (pode ser um UUID do cliente)
     * @param message    texto enviado pelo usuário
     * @return           resposta em texto para exibir no chat
     */
    String answer(String sessionId, String message);
}
