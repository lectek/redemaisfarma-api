package br.com.redemaisfarma.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.ollama")
public class AppAiOllamaProperties {
    /** Liga/desliga o uso do Ollama. */
    private boolean enabled = true;
    /** Base URL do serviço Ollama (ex.: http://localhost:11434). */
    private String baseUrl = "http://localhost:11434";
    /** Modelo (ex.: llama3.1:8b-instruct, qwen2.5:7b-instruct). */
    private String model = "llama3.1:8b-instruct";
    /** Temperatura (0 = mais determinístico). */
    private double temperature = 0.2;
    /** top_p (opcional). */
    private double topP = 0.9;
    /** Timeout em ms para chamada HTTP. */
    private int timeoutMs = 5000;
    /** Instruções de sistema (tom/foco do assistente). */
    private String systemPrompt = """
            Você é um assistente de ecommerce da RedeMaisFarma. 
            Responda em português claro e objetivo. 
            Se pedirem diagnóstico médico, oriente a procurar um profissional e ofereça produtos OTC relacionados.
            """;

    // getters/setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public double getTopP() { return topP; }
    public void setTopP(double topP) { this.topP = topP; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
}
