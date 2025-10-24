/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="app.ai.ollama")
public class AppAiOllamaProperties {
    private boolean enabled = true;
    private String baseUrl = "http://localhost:11434";
    private String model = "llama3.1:8b-instruct";
    private double temperature = 0.2;
    private double topP = 0.9;
    private int timeoutMs = 5000;
    private String systemPrompt = "Voc\u00ea \u00e9 um assistente de ecommerce da RedeMaisFarma.\nResponda em portugu\u00eas claro e objetivo.\nSe pedirem diagn\u00f3stico m\u00e9dico, oriente a procurar um profissional e ofere\u00e7a produtos OTC relacionados.\n";

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTopP() {
        return this.topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public int getTimeoutMs() {
        return this.timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getSystemPrompt() {
        return this.systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}

