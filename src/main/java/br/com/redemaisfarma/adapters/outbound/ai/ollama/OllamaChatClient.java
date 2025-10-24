/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.config.AppAiOllamaProperties
 *  com.fasterxml.jackson.databind.JsonNode
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.fasterxml.jackson.databind.node.ArrayNode
 *  com.fasterxml.jackson.databind.node.ObjectNode
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.outbound.ai.ollama;

import br.com.redemaisfarma.application.config.AppAiOllamaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class OllamaChatClient {
    private final AppAiOllamaProperties props;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaChatClient(AppAiOllamaProperties props) {
        this.props = props;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(props.getTimeoutMs())).build();
    }

    public String chatOnce(String sessionId, String userMessage) throws Exception {
        String url = this.props.getBaseUrl().replaceAll("/+$", "") + "/api/chat";
        ObjectNode root = this.mapper.createObjectNode();
        root.put("model", this.props.getModel());
        root.put("stream", false);
        root.put("temperature", this.props.getTemperature());
        root.put("top_p", this.props.getTopP());
        ArrayNode messages = root.putArray("messages");
        if (this.props.getSystemPrompt() != null && !this.props.getSystemPrompt().isBlank()) {
            ObjectNode systemMsg = this.mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", this.props.getSystemPrompt());
            messages.add((JsonNode)systemMsg);
        }
        ObjectNode userMsg = this.mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage == null ? "" : userMessage);
        messages.add((JsonNode)userMsg);
        byte[] body = this.mapper.writeValueAsBytes((Object)root);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMillis(this.props.getTimeoutMs())).header("Content-Type", "application/json; charset=utf-8").POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
        HttpResponse<byte[]> res = this.http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("Ollama HTTP " + res.statusCode() + ": " + new String(res.body(), StandardCharsets.UTF_8));
        }
        JsonNode json = this.mapper.readTree(res.body());
        JsonNode msg = json.path("message").path("content");
        if (msg.isMissingNode() || msg.asText().isBlank()) {
            return "Sem resposta no momento. Tente novamente.";
        }
        return msg.asText();
    }
}

