package br.com.redemaisfarma.adapters.outbound.ai.ollama;

import br.com.redemaisfarma.application.config.AppAiOllamaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Cliente simples usando /api/chat do Ollama (não-stream).
 * Docs: POST {base}/api/chat
 * body: { "model": "...", "messages":[{"role":"system","content":"..."},{"role":"user","content":"..."}] }
 */
@Component
public class OllamaChatClient {

    private final AppAiOllamaProperties props;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaChatClient(AppAiOllamaProperties props) {
        this.props = props;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .build();
    }

    /** Envia uma pergunta e retorna o texto da resposta. */
    public String chatOnce(String sessionId, String userMessage) throws Exception {
        String url = props.getBaseUrl().replaceAll("/+$","") + "/api/chat";

        ObjectNode root = mapper.createObjectNode();
        root.put("model", props.getModel());
        root.put("stream", false);
        root.put("temperature", props.getTemperature());
        root.put("top_p", props.getTopP());

        ArrayNode messages = root.putArray("messages");
        if (props.getSystemPrompt() != null && !props.getSystemPrompt().isBlank()) {
            ObjectNode systemMsg = mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", props.getSystemPrompt());
            messages.add(systemMsg);
        }
        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage == null ? "" : userMessage);
        messages.add(userMsg);

        byte[] body = mapper.writeValueAsBytes(root);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(props.getTimeoutMs()))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("Ollama HTTP " + res.statusCode() + ": " + new String(res.body(), StandardCharsets.UTF_8));
        }

        JsonNode json = mapper.readTree(res.body());
        // Resposta típica: { "message": { "role":"assistant", "content":"..." }, ... }
        JsonNode msg = json.path("message").path("content");
        if (msg.isMissingNode() || msg.asText().isBlank()) {
            return "Sem resposta no momento. Tente novamente.";
        }
        return msg.asText();
    }
}
